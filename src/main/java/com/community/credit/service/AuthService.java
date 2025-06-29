package com.community.credit.service;

import com.community.credit.dto.LoginRequest;
import com.community.credit.dto.LoginResponse;
import com.community.credit.dto.RegisterRequest;

/**
 * 认证服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 通过用户名重置密码
     */
    void resetPasswordByUsername(String username, String newPassword);

    /**
     * 验证用户名是否存在
     */
    boolean verifyUsernameExists(String username);
} 