package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.entity.CreditBehaviorType;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.CreditBehaviorTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 信用行为类型控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "信用行为类型", description = "信用行为类型管理接口")
@RestController
@RequestMapping("/credit-behavior-type")
public class CreditBehaviorTypeController {

    @Autowired
    private CreditBehaviorTypeService creditBehaviorTypeService;

    @Operation(summary = "获取所有启用的行为类型", description = "获取所有启用的信用行为类型列表")
    @GetMapping("/active")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<List<CreditBehaviorType>> getActiveBehaviorTypes() {
        List<CreditBehaviorType> behaviorTypes = creditBehaviorTypeService.getActiveBehaviorTypes();
        return Result.success("查询成功", behaviorTypes);
    }

    @Operation(summary = "根据分类获取行为类型", description = "根据守信/失信分类获取行为类型")
    @GetMapping("/category/{category}")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<List<CreditBehaviorType>> getBehaviorTypesByCategory(
            @PathVariable CreditBehaviorType.BehaviorCategory category) {
        List<CreditBehaviorType> behaviorTypes = creditBehaviorTypeService.getBehaviorTypesByCategory(category);
        return Result.success("查询成功", behaviorTypes);
    }

    @Operation(summary = "获取所有行为类型", description = "获取所有行为类型列表（管理员用）")
    @GetMapping("/all")
    @RequireRole({User.UserRole.ADMIN})
    public Result<List<CreditBehaviorType>> getAllBehaviorTypes() {
        List<CreditBehaviorType> behaviorTypes = creditBehaviorTypeService.list();
        return Result.success("查询成功", behaviorTypes);
    }

    @Operation(summary = "获取行为类型详情", description = "根据ID获取行为类型详情")
    @GetMapping("/{id}")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<CreditBehaviorType> getBehaviorType(@PathVariable Integer id) {
        CreditBehaviorType behaviorType = creditBehaviorTypeService.getById(id);
        if (behaviorType == null) {
            return Result.error(404, "行为类型不存在");
        }
        return Result.success("获取成功", behaviorType);
    }
} 