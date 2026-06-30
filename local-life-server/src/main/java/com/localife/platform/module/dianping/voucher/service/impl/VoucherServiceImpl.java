package com.localife.platform.module.dianping.voucher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.common.constant.RedisConstants;
import com.localife.platform.common.exception.BusinessException;
import com.localife.platform.config.RabbitMQConfig;
import com.localife.platform.module.dianping.voucher.dto.VoucherDTO;
import com.localife.platform.module.dianping.voucher.entity.SeckillVoucher;
import com.localife.platform.module.dianping.voucher.entity.Voucher;
import com.localife.platform.module.dianping.voucher.entity.VoucherOrder;
import com.localife.platform.module.dianping.voucher.mapper.SeckillVoucherMapper;
import com.localife.platform.module.dianping.voucher.mapper.VoucherMapper;
import com.localife.platform.module.dianping.voucher.mapper.VoucherOrderMapper;
import com.localife.platform.module.dianping.voucher.service.VoucherService;
import com.localife.platform.module.user.entity.User;
import com.localife.platform.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {

    private final SeckillVoucherMapper seckillVoucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final UserMapper userMapper;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    // ==================== 普通券购买（乐观锁） ====================

    @Override
    @Transactional
    public VoucherOrder buyVoucher(Long voucherId, Long userId) {
        Voucher voucher = getById(voucherId);
        if (voucher == null || voucher.getStatus() == 0) {
            throw new BusinessException("优惠券不存在或已下架");
        }
        if (voucher.getType() == 1) {
            throw new BusinessException("秒杀券请走秒杀通道");
        }

        // 库存扣减（乐观锁，stock=null 表示不限量）
        if (voucher.getStock() != null) {
            boolean success = lambdaUpdate()
                    .eq(Voucher::getId, voucherId)
                    .gt(Voucher::getStock, 0)
                    .setSql("stock = stock - 1")
                    .update();
            if (!success) {
                throw new BusinessException("已售罄");
            }
        }

        VoucherOrder order = new VoucherOrder();
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now());
        voucherOrderMapper.insert(order);
        return order;
    }

    // ==================== 秒杀 ====================

    @Override
    @Transactional
    public Long seckillVoucher(Long voucherId, Long userId) {
        // 1. 查询秒杀券信息
        SeckillVoucher seckill = seckillVoucherMapper.selectById(voucherId);
        if (seckill == null) {
            throw new BusinessException("秒杀券不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckill.getBeginTime())) {
            throw new BusinessException("秒杀尚未开始");
        }
        if (now.isAfter(seckill.getEndTime())) {
            throw new BusinessException("秒杀已结束");
        }
        // 2. 一人一单防重
        String orderKey = RedisConstants.SECKILL_ORDER_KEY + userId + ":" + voucherId;
        long ttl = java.time.Duration.between(now, seckill.getEndTime()).getSeconds();
        Boolean first = redisTemplate.opsForValue()
                .setIfAbsent(orderKey, "1", ttl, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(first)) {
            throw new BusinessException("您已抢购过该秒杀券");
        }
        // 2.5 DB 兜底防重复（Redis 可能被清空）
        if (voucherOrderMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<VoucherOrder>()
                        .eq(VoucherOrder::getUserId, userId)
                        .eq(VoucherOrder::getVoucherId, voucherId)) > 0) {
            throw new BusinessException("您已抢购过该秒杀券");
        }

        // 3. Redisson 分布式锁（库存校验移到锁内防超卖）
        String lockKey = RedisConstants.LOCK_SECKILL_KEY + voucherId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("系统繁忙，请稍后再试");
            }

            // 4. 锁内重新校验 DB 库存（防止超卖）
            SeckillVoucher latest = seckillVoucherMapper.selectById(voucherId);
            if (latest == null || latest.getStock() <= 0) {
                redisTemplate.delete(orderKey);
                throw new BusinessException("已售罄");
            }

            // 5. Redis 库存不存在则从 DB 初始化
            String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
            if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
                redisTemplate.opsForValue().set(stockKey, String.valueOf(latest.getStock()));
            }
            // 6. Lua 脚本原子扣库存
            Long result = redisTemplate.execute(SECKILL_SCRIPT, Collections.singletonList(stockKey));
            if (result == null || result == 0) {
                redisTemplate.delete(orderKey);
                throw new BusinessException("已售罄");
            }

            // 6. 条件 SQL 原子更新 DB 库存（防负数兜底）
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SeckillVoucher> uw =
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SeckillVoucher>()
                    .eq(SeckillVoucher::getVoucherId, voucherId)
                    .gt(SeckillVoucher::getStock, 0)
                    .setSql("stock = stock - 1");
            int rows = seckillVoucherMapper.update(uw);
            if (rows == 0) {
                redisTemplate.delete(orderKey);
                throw new BusinessException("已售罄");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 6. RabbitMQ 异步下单
        VoucherOrder order = new VoucherOrder();
        order.setId(nextOrderId());
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now());
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY, order);

        return order.getId();
    }

    // ==================== 券支付 / 退款 / 核销 ====================

    @Override
    @Transactional
    public void payVoucherOrder(Long orderId, Long userId) {
        VoucherOrder order = voucherOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("订单不属于当前用户");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态异常，无法支付");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        voucherOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void refundVoucherOrder(Long orderId, Long userId) {
        VoucherOrder order = voucherOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("订单不属于当前用户");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态异常，无法退款");
        }
        order.setStatus(2); // 已退款
        voucherOrderMapper.updateById(order);

        // 恢复库存
        Long voucherId = order.getVoucherId();
        Voucher voucher = getById(voucherId);
        if (voucher != null && voucher.getType() != null && voucher.getType() == 1) {
            // 秒杀券：恢复 tb_seckill_voucher.stock
            SeckillVoucher sv = seckillVoucherMapper.selectById(voucherId);
            if (sv != null) {
                sv.setStock(sv.getStock() + 1);
                seckillVoucherMapper.updateById(sv);
                // 同步 Redis 库存
                String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
                redisTemplate.opsForValue().set(stockKey, String.valueOf(sv.getStock()));
            }
        } else if (voucher != null && voucher.getStock() != null) {
            // 普通券：恢复 tb_voucher.stock
            lambdaUpdate().eq(Voucher::getId, voucherId)
                    .setSql("stock = stock + 1").update();
        }
    }

    @Override
    @Transactional
    public void verifyVoucherOrder(Long orderId, Long shopId) {
        VoucherOrder order = voucherOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 校验券归属当前商家店铺
        Voucher voucher = getById(order.getVoucherId());
        if (voucher == null || !voucher.getShopId().equals(shopId)) {
            throw new BusinessException("该券不属于您的店铺");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态异常，无法核销");
        }
        order.setStatus(3); // 已核销
        voucherOrderMapper.updateById(order);
    }

    // ==================== 商家管理 ====================

    @Override
    @Transactional
    public void createVoucher(VoucherDTO dto) {
        Voucher voucher = new Voucher();
        voucher.setShopId(dto.getShopId());
        voucher.setTitle(dto.getTitle());
        voucher.setSubTitle(dto.getSubTitle());
        voucher.setRules(dto.getRules());
        voucher.setPayValue(dto.getPayValue());
        voucher.setActualValue(dto.getActualValue());
        voucher.setStatus(1);
        voucher.setCreateTime(LocalDateTime.now());

        // 判断是否创建秒杀券
        if (dto.getBeginTime() != null) {
            voucher.setStock(null); // 秒杀券库存走 tb_seckill_voucher
            voucher.setType(1); // 直接标记为秒杀券，避免普通券窗口期
            save(voucher);
            convertToSeckill(voucher.getId(), dto);
        } else {
            voucher.setStock(dto.getStock()); // 普通券库存
            voucher.setType(0);
            save(voucher);
        }
    }

    @Override
    @Transactional
    public void convertToSeckill(Long voucherId, VoucherDTO dto) {
        Voucher voucher = getById(voucherId);
        if (voucher == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 更新券类型为秒杀
        voucher.setType(1);
        voucher.setPayValue(dto.getPayValue() != null ? dto.getPayValue() : voucher.getPayValue());
        updateById(voucher);

        // 创建/更新秒杀券记录
        SeckillVoucher seckill = seckillVoucherMapper.selectById(voucherId);
        boolean isNew = (seckill == null);
        if (isNew) {
            seckill = new SeckillVoucher();
            seckill.setVoucherId(voucherId);
        }
        seckill.setStock(dto.getStock());
        seckill.setBeginTime(dto.getBeginTime());
        seckill.setEndTime(dto.getEndTime());
        if (isNew) {
            seckillVoucherMapper.insert(seckill);
        } else {
            seckillVoucherMapper.updateById(seckill);
        }

        // 秒杀库存预热到 Redis
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(dto.getStock()));
    }

    @Override
    public Page<Voucher> pageVouchers(Long shopId, Integer type, int page, int size) {
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<Voucher>()
                .eq(shopId != null, Voucher::getShopId, shopId)
                .eq(type != null, Voucher::getType, type)
                .orderByDesc(Voucher::getCreateTime);
        Page<Voucher> result = page(new Page<>(page, size), wrapper);

        // 填充秒杀券的库存和时间
        java.util.List<Long> seckillIds = result.getRecords().stream()
                .filter(v -> v.getType() != null && v.getType() == 1)
                .map(Voucher::getId).toList();
        if (!seckillIds.isEmpty()) {
            java.util.Map<Long, SeckillVoucher> seckillMap = seckillVoucherMapper.selectBatchIds(seckillIds).stream()
                    .collect(java.util.stream.Collectors.toMap(SeckillVoucher::getVoucherId, s -> s));
            result.getRecords().forEach(v -> {
                if (v.getType() == 1) {
                    SeckillVoucher sv = seckillMap.get(v.getId());
                    if (sv != null) {
                        v.setSeckillStock(sv.getStock());
                        v.setSeckillBeginTime(sv.getBeginTime());
                        v.setSeckillEndTime(sv.getEndTime());
                    }
                }
            });
        }
        return result;
    }

    @Override
    public Page<VoucherOrder> pageUserOrders(Long userId, int page, int size) {
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .orderByDesc(VoucherOrder::getCreateTime);
        Page<VoucherOrder> result = voucherOrderMapper.selectPage(new Page<>(page, size), wrapper);
        fillVoucherInfo(result.getRecords());
        return result;
    }

    @Override
    public Page<VoucherOrder> pageShopOrders(Long shopId, int page, int size) {
        LambdaQueryWrapper<Voucher> vWrapper = new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getShopId, shopId)
                .select(Voucher::getId);
        java.util.List<Long> voucherIds = list(vWrapper).stream().map(Voucher::getId).toList();
        if (voucherIds.isEmpty()) {
            return new Page<>(page, size, 0);
        }
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<VoucherOrder>()
                .in(VoucherOrder::getVoucherId, voucherIds)
                .orderByDesc(VoucherOrder::getCreateTime);
        Page<VoucherOrder> result = voucherOrderMapper.selectPage(new Page<>(page, size), wrapper);

        // 填充用户手机号
        java.util.Set<Long> userIds = result.getRecords().stream()
                .map(VoucherOrder::getUserId).collect(java.util.stream.Collectors.toSet());
        if (!userIds.isEmpty()) {
            java.util.Map<Long, String> phoneMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(java.util.stream.Collectors.toMap(User::getId, u -> u.getPhone() != null ? u.getPhone() : "未知"));
            result.getRecords().forEach(o -> o.setUserPhone(phoneMap.getOrDefault(o.getUserId(), "未知")));
        }
        fillVoucherInfo(result.getRecords());
        return result;
    }

    /**
     * 填充券订单关联的券信息（标题、金额、面值、店铺）
     */
    private void fillVoucherInfo(java.util.List<VoucherOrder> orders) {
        if (orders.isEmpty()) return;
        java.util.Set<Long> vIds = orders.stream().map(VoucherOrder::getVoucherId).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, Voucher> vMap = listByIds(vIds).stream()
                .collect(java.util.stream.Collectors.toMap(Voucher::getId, v -> v));
        orders.forEach(o -> {
            Voucher v = vMap.get(o.getVoucherId());
            if (v != null) {
                o.setVoucherTitle(v.getTitle());
                o.setPayValue(v.getPayValue());
                o.setActualValue(v.getActualValue());
                o.setShopId(v.getShopId());
            }
        });
    }

    // ==================== 简单ID生成 ====================

    private long nextOrderId() {
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }
}
