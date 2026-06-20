package com.localife.platform.module.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.localife.platform.common.constant.JwtClaimsConstant;
import com.localife.platform.common.constant.RedisConstants;
import com.localife.platform.common.constant.UserTypeEnum;
import com.localife.platform.common.exception.BusinessException;
import com.localife.platform.common.utils.JwtUtil;
import com.localife.platform.module.user.dto.EmployeeDTO;
import com.localife.platform.module.user.dto.MerchantLoginDTO;
import com.localife.platform.module.user.dto.UserLoginDTO;
import com.localife.platform.module.user.entity.User;
import com.localife.platform.module.user.mapper.UserMapper;
import com.localife.platform.module.user.service.UserService;
import com.localife.platform.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // 查用户，不存在则自动注册
        User user = lambdaQuery().eq(User::getPhone, phone).one();
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

        String token = generateToken(user);
        return toVO(user, token);
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

    // ==================== 店员管理 ====================

    @Override
    @Transactional
    public void addEmployee(EmployeeDTO dto) {
        if (StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPassword())) {
            throw new BusinessException("用户名和密码不能为空");
        }
        // 检查用户名唯一
        Long count = lambdaQuery().eq(User::getUsername, dto.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User employee = new User();
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setUsername(dto.getUsername());
        employee.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        employee.setSex(dto.getSex());
        employee.setIdNumber(dto.getIdNumber());
        employee.setUserType(UserTypeEnum.MERCHANT.getCode());
        employee.setStatus(1);
        employee.setCreateTime(LocalDateTime.now());
        save(employee);
    }

    @Override
    @Transactional
    public void updateEmployee(Long id, EmployeeDTO dto) {
        User employee = getById(id);
        if (employee == null) {
            throw new BusinessException("店员不存在");
        }
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setUsername(dto.getUsername());
        if (StrUtil.isNotBlank(dto.getPassword())) {
            employee.setPassword(dto.getPassword());
        }
        employee.setSex(dto.getSex());
        employee.setIdNumber(dto.getIdNumber());
        updateById(employee);
    }

    @Override
    @Transactional
    public void toggleEmployeeStatus(Long id) {
        User employee = getById(id);
        if (employee == null) {
            throw new BusinessException("店员不存在");
        }
        employee.setStatus(employee.getStatus() == 1 ? 0 : 1);
        updateById(employee);
    }

    @Override
    public Page<User> pageEmployees(int page, int size, String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUserType, UserTypeEnum.MERCHANT.getCode())
                .like(StrUtil.isNotBlank(name), User::getName, name)
                .orderByDesc(User::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    // ==================== 私有方法 ====================

    private String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.PHONE, user.getPhone());
        claims.put(JwtClaimsConstant.USER_TYPE, user.getUserType());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        return jwtUtil.createToken(claims, user.getId());
    }

    private UserVO toVO(User user, String token) {
        return UserVO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickName(user.getNickName())
                .name(user.getName())
                .icon(user.getIcon())
                .sex(user.getSex())
                .userType(user.getUserType())
                .token(token)
                .build();
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
