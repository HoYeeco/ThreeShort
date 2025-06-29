package com.community.credit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.credit.common.Result;
import com.community.credit.dto.UserQueryRequest;
import com.community.credit.dto.UserUpdateRequest;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器 - 简化版（无JWT认证）
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "用户管理", description = "用户信息查询、更新等管理接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{userId}")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<User> getUserInfo(@PathVariable Integer userId) {
        User user = userService.getUserDetail(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("获取成功", user);
    }

    @Operation(summary = "更新用户信息", description = "更新指定用户的基本信息")
    @PutMapping("/{userId}")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<String> updateUserInfo(@PathVariable Integer userId,
                                        @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(userId, request);
        return Result.success("更新成功");
    }

    @Operation(summary = "用户列表查询", description = "分页查询用户列表")
    @GetMapping("/list")
    @RequireRole({User.UserRole.ADMIN})
    public Result<IPage<User>> getUserList(@Valid UserQueryRequest request) {
        IPage<User> userPage = userService.queryUsers(request);
        return Result.success("查询成功", userPage);
    }

    @Operation(summary = "启用/禁用用户", description = "启用或禁用用户账户")
    @PutMapping("/{userId}/status")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> updateUserStatus(@PathVariable Integer userId,
                                          @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            return Result.error(400, "状态参数错误");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        user.setStatus(status);
        userService.updateById(user);

        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }

    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    @PutMapping("/{userId}/password")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> resetUserPassword(@PathVariable Integer userId,
                                           @RequestParam String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error(400, "新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        userService.resetPassword(userId, newPassword);
        return Result.success("密码重置成功");
    }

    @Operation(summary = "修改自己的密码", description = "用户修改自己的密码")
    @PutMapping("/{userId}/change-password")
    @RequireRole(value = {User.UserRole.RESIDENT, User.UserRole.ADMIN, User.UserRole.MAINTAINER}, allowSelf = true)
    public Result<String> changePassword(@PathVariable Integer userId,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam(value = "currentUserId", defaultValue = "1") Integer currentUserId) {
        // 权限检查：只能修改自己的密码
        if (!userId.equals(currentUserId)) {
            return Result.error(403, "只能修改自己的密码");
        }

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.error(400, "原密码不能为空");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error(400, "新密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }

        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新用户角色", description = "管理员更新指定用户角色")
    @PutMapping("/{userId}/role")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> updateUserRole(@PathVariable Integer userId,
                                        @RequestParam User.UserRole role) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        user.setRole(role);
        userService.updateById(user);
        return Result.success("角色更新成功");
    }

    @Operation(summary = "获取用户统计数据", description = "获取Dashboard用户统计信息")
    @GetMapping("/statistics")
    @RequireRole({User.UserRole.ADMIN, User.UserRole.RESIDENT})
    public Result<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = userService.getUserStatistics();
        return Result.success("获取成功", stats);
    }
} 