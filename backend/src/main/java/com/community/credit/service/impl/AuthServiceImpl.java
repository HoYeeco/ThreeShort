package com.community.credit.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.community.credit.dto.LoginRequest;
import com.community.credit.dto.LoginResponse;
import com.community.credit.dto.RegisterRequest;
import com.community.credit.entity.User;
import com.community.credit.entity.UserCreditProfile;
import com.community.credit.mapper.UserMapper;
import com.community.credit.service.AuthService;
import com.community.credit.service.SystemLogService;
import com.community.credit.service.UserCreditProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCreditProfileService userCreditProfileService;

    @Autowired
    private SystemLogService systemLogService;

    @Value("${app.default-credit-score:60}")
    private Integer defaultCreditScore;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            // 获取用户信息
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 明文密码校验
            if (!request.getPassword().equals(user.getPassword())) {
                throw new RuntimeException("用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("用户已被禁用");
            }

            // 构建响应（去掉JWT token）
            LoginResponse response = new LoginResponse();
            response.setAccessToken("no-token-needed"); // 简化版本不需要token
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setRole(user.getRole());
            response.setAvatar(user.getAvatar());
            response.setCommunityId(user.getCommunityId());

            // 记录登录成功日志
            systemLogService.recordSuccessLog(
                user.getId(),
                "USER_LOGIN",
                String.format("用户 %s（%s）登录成功", user.getUsername(), user.getRealName()),
                "POST",
                "/auth/login",
                String.format("{\"username\":\"%s\"}", request.getUsername()),
                String.format("{\"userId\":%d,\"role\":\"%s\"}", user.getId(), user.getRole())
            );

            log.info("用户登录成功: {}", user.getUsername());
            return response;

        } catch (Exception e) {
            // 记录登录失败日志
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
            Integer userId = user != null ? user.getId() : null;
            
            systemLogService.recordFailureLog(
                userId,
                "USER_LOGIN",
                String.format("用户 %s 登录失败：%s", request.getUsername(), e.getMessage()),
                "POST",
                "/auth/login",
                String.format("{\"username\":\"%s\"}", request.getUsername()),
                e.getMessage()
            );

            log.error("用户登录失败: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername()));
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        User existingPhoneUser = userMapper.selectOne(new QueryWrapper<User>().eq("phone", request.getPhone()));
        if (existingPhoneUser != null) {
            throw new RuntimeException("手机号已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 明文密码存储
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        if (request.getIdCard() != null && !request.getIdCard().isEmpty()) {
            user.setIdCard(request.getIdCard());
        }
        user.setAddress(request.getAddress());
        user.setRole(User.UserRole.RESIDENT);
        user.setStatus(1);
        user.setCommunityId(request.getCommunityId());

        userMapper.insert(user);

        // 创建用户信用档案
        UserCreditProfile profile = new UserCreditProfile();
        profile.setUserId(user.getId());
        profile.setCurrentScore(defaultCreditScore);
        profile.setRewardPoints(0);
        profile.setCreditLevel("C");
        profile.setTotalReports(0);
        profile.setApprovedReports(0);

        userCreditProfileService.save(profile);

        // 记录注册成功日志
        systemLogService.recordSuccessLog(
            user.getId(),
            "USER_REGISTER",
            String.format("新用户 %s（%s）注册成功", request.getUsername(), request.getRealName()),
            "POST",
            "/auth/register",
            String.format("{\"username\":\"%s\",\"realName\":\"%s\",\"phone\":\"%s\"}", 
                request.getUsername(), request.getRealName(), request.getPhone()),
            String.format("{\"userId\":%d,\"creditScore\":%d}", user.getId(), defaultCreditScore)
        );

        log.info("用户注册成功: {}", request.getUsername());
    }

    @Override
    public void resetPasswordByUsername(String username, String newPassword) {
        // 验证用户名格式
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }

        // 验证密码长度
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        // 查找用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 更新密码
        user.setPassword(newPassword); // 明文密码存储
        userMapper.updateById(user);

        log.info("用户密码重置成功: {}", user.getUsername());
    }

    @Override
    public boolean verifyUsernameExists(String username) {
        // 验证用户名格式
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }

        // 查找用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user != null;
    }

} 