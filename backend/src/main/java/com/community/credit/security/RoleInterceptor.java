package com.community.credit.security;

import com.community.credit.entity.User;
import com.community.credit.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色权限拦截器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理控制器方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // 检查方法上的权限注解
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        if (requireRole == null) {
            // 检查类上的权限注解
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 如果没有权限注解，直接通过
        if (requireRole == null) {
            return true;
        }

        // 获取当前用户
        HttpSession session = request.getSession(false);
        if (session == null) {
            return handleUnauthorized(response, "未登录");
        }

        Integer currentUserId = (Integer) session.getAttribute("userId");
        if (currentUserId == null) {
            return handleUnauthorized(response, "未登录");
        }

        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            return handleUnauthorized(response, "用户不存在");
        }

        // 检查角色权限
        User.UserRole[] requiredRoles = requireRole.value();
        boolean hasRole = Arrays.asList(requiredRoles).contains(currentUser.getRole());

        if (!hasRole) {
            // 如果允许访问自己的资源，检查是否是访问自己的资源
            if (requireRole.allowSelf()) {
                String pathInfo = request.getPathInfo();
                if (pathInfo != null && pathInfo.contains("/" + currentUserId + "/")) {
                    return true;
                }
                
                // 检查路径参数
                String userId = request.getParameter("userId");
                if (userId != null && currentUserId.toString().equals(userId)) {
                    return true;
                }
            }
            
            return handleForbidden(response, "权限不足");
        }

        return true;
    }

    private boolean handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }

    private boolean handleForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", message);
        result.put("data", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
} 