package com.mishi.platform.module.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mishi.platform.common.constant.JwtClaimsConstant;
import com.mishi.platform.common.constant.RedisConstants;
import com.mishi.platform.common.constant.UserTypeEnum;
import com.mishi.platform.common.exception.BusinessException;
import com.mishi.platform.common.utils.JwtUtil;
import com.mishi.platform.module.user.dto.MerchantLoginDTO;
import com.mishi.platform.module.user.dto.MerchantRegisterDTO;
import com.mishi.platform.module.user.dto.UserLoginDTO;
import com.mishi.platform.module.user.dto.WxLoginDTO;
import com.mishi.platform.module.user.entity.User;
import com.mishi.platform.module.user.mapper.UserMapper;
import com.mishi.platform.module.user.service.UserService;
import com.mishi.platform.module.user.vo.UserVO;
import com.mishi.platform.module.shop.entity.Shop;
import com.mishi.platform.module.shop.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ShopMapper shopMapper;

    @Value("${wechat.mock-openid:true}")
    private boolean wechatMockOpenid;

    // ==================== 顾客登录 ====================

    @Override
    public UserVO loginByPhone(UserLoginDTO dto) {
        String phone = dto.getPhone();
        String code = dto.getCode();

        if (StrUtil.isBlank(phone) || StrUtil.isBlank(code)) {
            throw new BusinessException("手机号或验证码不能为空");
        }

        // 校验验证码（开发环境固定 123456，Redis 不可用时跳过缓存校验）
        String cacheCode = null;
        try {
            cacheCode = redisTemplate.opsForValue()
                    .get(RedisConstants.SMS_CODE_KEY + phone);
        } catch (Exception ignored) {
            // Redis 未启动时忽略
        }
        if (!"123456".equals(code) && !code.equals(cacheCode)) {
            throw new BusinessException("验证码错误");
        }

        // 查用户（仅限顾客），不存在则自动注册
        User user = lambdaQuery()
                .eq(User::getPhone, phone)
                .eq(User::getUserType, UserTypeEnum.CUSTOMER.getCode()).one();
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickName("用户" + RandomUtil.randomNumbers(6));
            user.setUserType(UserTypeEnum.CUSTOMER.getCode());
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            save(user);
            log.info("新用户注册: phone={}", phone);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 签发 JWT
        String token = generateToken(user);
        // 删除验证码
        try {
            redisTemplate.delete(RedisConstants.SMS_CODE_KEY + phone);
        } catch (Exception ignored) {
            // Redis 未启动时忽略
        }

        return toVO(user, token);
    }

    // ==================== 微信登录 ====================

    @Override
    public UserVO loginByWx(WxLoginDTO dto) {
        String code = dto.getCode();
        String openid;

        if (wechatMockOpenid) {
            // 开发环境：用 code 的 hash 模拟唯一 openid
            openid = "mock_openid_" + Math.abs(code.hashCode() % 100000);
            log.info("开发环境 mock openid: code={} → openid={}", code, openid);
        } else {
            // 生产环境：调微信 code2Session 接口换取 openid
            openid = fetchOpenidFromWx(code);
        }

        // 用 openid 查用户，不存在则自动注册
        User user = lambdaQuery()
                .eq(User::getOpenid, openid)
                .eq(User::getUserType, UserTypeEnum.CUSTOMER.getCode()).one();
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickName("微信用户" + RandomUtil.randomNumbers(6));
            user.setUserType(UserTypeEnum.CUSTOMER.getCode());
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            save(user);
            log.info("微信新用户注册: openid={}", openid);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = generateToken(user);
        return toVO(user, token);
    }

    // ==================== 商家登录 ====================

    @Override
    public UserVO loginByPassword(MerchantLoginDTO dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new BusinessException("用户名或密码不能为空");
        }

        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getUserType, UserTypeEnum.MERCHANT.getCode())
                .one();

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // BCrypt 密码校验
        if (!checkPw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 查商家店铺
        Shop shop = shopMapper.selectOne(
                new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantUserId, user.getId()));
        String token = generateToken(user, shop != null ? shop.getId() : null);
        UserVO vo = toVO(user, token);
        if (shop != null) vo.setShopId(shop.getId());
        return vo;
    }

    // ==================== 商家注册 ====================

    @Override
    @Transactional
    public UserVO register(MerchantRegisterDTO dto) {
        if (StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        if (StrUtil.isBlank(dto.getShopName())) {
            throw new BusinessException("店铺名称不能为空");
        }
        // 检查用户名唯一
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 检查手机号唯一（仅限商家）
        if (StrUtil.isNotBlank(dto.getPhone())
                && lambdaQuery().eq(User::getPhone, dto.getPhone())
                        .eq(User::getUserType, UserTypeEnum.MERCHANT.getCode()).count() > 0) {
            throw new BusinessException("该手机号已被商家注册");
        }

        // 1. 创建商家用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setPhone(dto.getPhone());
        user.setName(dto.getName());
        user.setUserType(UserTypeEnum.MERCHANT.getCode());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        save(user);

        // 2. 创建店铺
        Shop shop = new Shop();
        shop.setName(dto.getShopName());
        shop.setTypeId(dto.getTypeId());
        shop.setMerchantUserId(user.getId());
        shop.setArea(dto.getArea());
        shop.setAddress(dto.getAddress());
        shop.setPhone(dto.getShopPhone());
        shop.setOpenHours(dto.getOpenHours());
        shop.setDescription(dto.getDescription());
        shop.setStatus(1);
        shop.setCreateTime(LocalDateTime.now());
        shopMapper.insert(shop);

        String token = generateToken(user, shop.getId());
        UserVO vo = toVO(user, token);
        vo.setShopId(shop.getId());
        return vo;
    }

    // ==================== 个人信息 ====================

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user, null);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String nickName, String icon) {
        User user = getById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (nickName != null) user.setNickName(nickName);
        if (icon != null) user.setIcon(icon);
        updateById(user);
    }

    // ==================== 私有方法 ====================

    private String generateToken(User user) {
        return generateToken(user, null);
    }

    private String generateToken(User user, Long shopId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        claims.put(JwtClaimsConstant.USER_TYPE, user.getUserType());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        if (shopId != null) claims.put(JwtClaimsConstant.SHOP_ID, shopId);
        return jwtUtil.createToken(claims, user.getId());
    }

    private UserVO toVO(User user, String token) {
        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickName(user.getNickName())
                .name(user.getName())
                .icon(user.getIcon())
                .userType(user.getUserType())
                .token(token)
                .build();
    }

    /**
     * 调用微信 code2Session 接口，用 code 换取 openid。
     * 文档: https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-login/code2Session.html
     */
    private String fetchOpenidFromWx(String code) {
        String appid = "wx62265c49944608a9";
        String secret = ""; // TODO: 填入你的小程序 AppSecret
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);
        String resp = cn.hutool.http.HttpUtil.get(url);
        cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(resp);
        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            throw new BusinessException("微信登录失败: " + json.getStr("errmsg"));
        }
        return json.getStr("openid");
    }

    /**
     * BCrypt 密码校验
     */
    private boolean checkPw(String raw, String hashed) {
        // 兼容已存在的明文密码（首次登录后自动升级为 BCrypt）
        if (raw.equals(hashed)) return true;
        try {
            return BCrypt.checkpw(raw, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
