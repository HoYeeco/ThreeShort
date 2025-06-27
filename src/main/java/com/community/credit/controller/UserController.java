package com.community.credit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.credit.common.Result;
import com.community.credit.dto.UserQueryRequest;
import com.community.credit.dto.UserUpdateRequest;
import com.community.credit.entity.User;
import com.community.credit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<User> getUserInfo(@PathVariable Integer userId) {
        User user = userService.getUserDetail(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success("获取成功", user);
    }

    @Operation(summary = "更新用户信息", description = "更新指定用户的基本信息")
    @PutMapping("/{userId}")
    public Result<String> updateUserInfo(@PathVariable Integer userId,
                                        @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(userId, request);
        return Result.success("更新成功");
    }

    @Operation(summary = "用户列表查询", description = "分页查询用户列表")
    @GetMapping("/list")
    public Result<IPage<User>> getUserList(@Valid UserQueryRequest request) {
        IPage<User> userPage = userService.queryUsers(request);
        return Result.success("查询成功", userPage);
    }

    @Operation(summary = "启用/禁用用户", description = "启用或禁用用户账户")
    @PutMapping("/{userId}/status")
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
} 