package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.dto.LoginRequest;
import com.community.credit.dto.LoginResponse;
import com.community.credit.dto.RegisterRequest;
import com.community.credit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "认证管理", description = "用户登录、注册、登出等认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = authService.login(request);
            
            // 设置Session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("userId", response.getUserId());
            session.setAttribute("username", response.getUsername());
            session.setAttribute("role", response.getRole().name());
            
            // 生成简单的Token（格式：userId:timestamp:hash）
            String token = generateSimpleToken(response.getUserId());
            response.setAccessToken(token);
            
            return Result.success("登录成功", response);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 生成简单的Token
     */
    private String generateSimpleToken(Integer userId) {
        long timestamp = System.currentTimeMillis();
        // 简单的hash计算
        String hash = String.valueOf((userId + ":" + timestamp + ":community-credit").hashCode());
        return userId + ":" + timestamp + ":" + hash;
    }

    @Operation(summary = "用户注册", description = "新用户注册")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "用户登出", description = "用户登出")
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success("登出成功");
    }

    @Operation(summary = "找回密码", description = "通过用户名找回密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam String username, @RequestParam String newPassword) {
        try {
            authService.resetPasswordByUsername(username, newPassword);
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "验证用户名", description = "验证用户名是否存在")
    @PostMapping("/verify-username")
    public Result<String> verifyUsername(@RequestParam String username) {
        try {
            boolean exists = authService.verifyUsernameExists(username);
            if (exists) {
                return Result.success("用户名验证成功");
            } else {
                return Result.error(404, "用户名不存在");
            }
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息")
    @GetMapping("/me")
    public Result<LoginResponse> getCurrentUser(HttpServletRequest request) {
        try {
            Integer currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return Result.error("请先登录");
            }
            
            // 这里可以从数据库获取完整的用户信息
            LoginResponse response = new LoginResponse();
            response.setUserId(currentUserId);
            response.setUsername("当前用户"); // 简化处理
            
            return Result.success("获取成功", response);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前用户ID - 支持Token和Session两种方式
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        // 1. 优先尝试从Token获取（Authorization header）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 简单的token解析 - 假设token格式为 "userId:timestamp:signature"
                String[] parts = token.split(":");
                if (parts.length >= 1) {
                    return Integer.valueOf(parts[0]);
                }
            } catch (Exception e) {
                // Token解析失败，继续尝试其他方式
            }
        }
        
        // 2. 尝试从URL参数获取token
        String tokenParam = request.getParameter("token");
        if (tokenParam != null) {
            try {
                String[] parts = tokenParam.split(":");
                if (parts.length >= 1) {
                    return Integer.valueOf(parts[0]);
                }
            } catch (Exception e) {
                // Token解析失败，继续尝试其他方式
            }
        }
        
        // 3. 回退到Session方式
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Integer) session.getAttribute("userId");
        }
        
        return null;
    }
} 