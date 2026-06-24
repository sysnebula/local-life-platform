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

        VoucherOrder order = new VoucherOrder();
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setPayType(0);
        order.setStatus(1); // 已支付
        order.setCreateTime(LocalDateTime.now());
        order.setPayTime(LocalDateTime.now());
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
        Boolean first = redisTemplate.opsForValue()
                .setIfAbsent(orderKey, "1", seckill.getEndTime().getSecond() - now.getSecond(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(first)) {
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

            // 6. 乐观 SQL 更新 DB 库存
            latest.setStock(latest.getStock() - 1);
            seckillVoucherMapper.updateById(latest);

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
        order.setPayType(0);
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        order.setPayTime(LocalDateTime.now());
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY, order);

        return order.getId();
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
        voucher.setType(0); // 默认普通券
        voucher.setStatus(1);
        voucher.setCreateTime(LocalDateTime.now());
        save(voucher);

        // 如果带了秒杀参数
        if (dto.getStock() != null && dto.getStock() > 0) {
            convertToSeckill(voucher.getId(), dto);
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

        // 创建秒杀券记录
        SeckillVoucher seckill = seckillVoucherMapper.selectById(voucherId);
        if (seckill == null) {
            seckill = new SeckillVoucher();
            seckill.setVoucherId(voucherId);
        }
        seckill.setStock(dto.getStock());
        seckill.setBeginTime(dto.getBeginTime());
        seckill.setEndTime(dto.getEndTime());
        SeckillVoucher exist = seckillVoucherMapper.selectById(voucherId);
        if (exist != null) {
            seckillVoucherMapper.updateById(seckill);
        } else {
            seckillVoucherMapper.insert(seckill);
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
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<VoucherOrder> pageUserOrders(Long userId, int page, int size) {
        LambdaQueryWrapper<VoucherOrder> wrapper = new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .orderByDesc(VoucherOrder::getCreateTime);
        return voucherOrderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 简单ID生成 ====================

    private long nextOrderId() {
        return System.currentTimeMillis() * 1000 + (long) (Math.random() * 1000);
    }
}
