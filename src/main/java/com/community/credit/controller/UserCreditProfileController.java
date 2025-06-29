package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.entity.User;
import com.community.credit.entity.UserCreditProfile;
import com.community.credit.security.RequireRole;
import com.community.credit.service.UserCreditProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户信用档案控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "用户信用档案", description = "信用档案查询、统计等管理接口")
@RestController
@RequestMapping("/user-credit-profile")
public class UserCreditProfileController {

    @Autowired
    private UserCreditProfileService userCreditProfileService;

    @Operation(summary = "获取用户信用档案", description = "根据用户ID获取信用档案信息")
    @GetMapping("/{userId}")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<UserCreditProfile> getUserProfile(@PathVariable Integer userId) {
        UserCreditProfile profile = userCreditProfileService.getUserProfile(userId);
        if (profile == null) {
            return Result.error(404, "用户信用档案不存在");
        }
        return Result.success("获取成功", profile);
    }

    @Operation(summary = "获取当前用户信用档案", description = "获取当前登录用户的信用档案")
    @GetMapping("/current")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<UserCreditProfile> getCurrentUserProfile(@RequestParam Integer userId) {
        UserCreditProfile profile = userCreditProfileService.getOrCreateUserProfile(userId);
        return Result.success("获取成功", profile);
    }

    @Operation(summary = "获取用户信用统计", description = "获取用户的信用统计数据")
    @GetMapping("/{userId}/stats")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<Map<String, Object>> getUserCreditStats(@PathVariable Integer userId) {
        Map<String, Object> stats = userCreditProfileService.getUserCreditStats(userId);
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "获取信用等级分布", description = "获取所有用户的信用等级分布统计")
    @GetMapping("/level-distribution")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Map<String, Object>> getCreditLevelDistribution() {
        Map<String, Object> distribution = userCreditProfileService.getCreditLevelDistribution();
        return Result.success("获取成功", distribution);
    }

    @Operation(summary = "获取信用排行榜", description = "获取信用分数排行榜")
    @GetMapping("/ranking")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<List<UserCreditProfile>> getCreditRanking(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<UserCreditProfile> ranking = userCreditProfileService.getCreditRanking(limit);
        return Result.success("获取成功", ranking);
    }

    @Operation(summary = "重新计算用户信用", description = "重新计算指定用户的信用分数")
    @PostMapping("/{userId}/recalculate")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> recalculateUserCredit(@PathVariable Integer userId) {
        userCreditProfileService.recalculateUserCredit(userId);
        return Result.success("重新计算完成");
    }

    @Operation(summary = "生成用户评分记录", description = "为指定用户生成周期评分记录")
    @PostMapping("/{userId}/score-record")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> generateScoreRecord(@PathVariable Integer userId,
                                            @RequestParam String period) {
        userCreditProfileService.generateScoreRecord(userId, period);
        return Result.success("评分记录生成完成");
    }

    @Operation(summary = "批量生成周期评分", description = "为所有用户生成周期评分记录")
    @PostMapping("/batch-generate-score")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> batchGenerateScoreRecord(@RequestParam String period) {
        userCreditProfileService.batchGenerateScoreRecord(period);
        return Result.success("批量评分记录生成完成");
    }

    @Operation(summary = "获取用户积分历史", description = "获取用户的积分变化历史")
    @GetMapping("/{userId}/score-history")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<List<Map<String, Object>>> getUserScoreHistory(@PathVariable Integer userId) {
        List<Map<String, Object>> history = userCreditProfileService.getUserScoreHistory(userId);
        return Result.success("获取成功", history);
    }

    @Operation(summary = "同步用户当前评分记录", description = "为用户生成或更新当前的评分记录，确保历史记录包含最新数据")
    @PostMapping("/{userId}/sync-current-score")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<String> syncCurrentScoreRecord(@PathVariable Integer userId) {
        try {
            // 通过服务层调用评分记录服务
            userCreditProfileService.generateScoreRecord(userId, 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return Result.success("同步成功");
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分页查询用户信用档案", description = "管理员分页查询用户信用档案列表")
    @GetMapping("/list")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Map<String, Object>> getUserCreditProfileList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String creditLevel) {
        
        Map<String, Object> result = userCreditProfileService.getUserCreditProfileList(page, size, keyword, creditLevel);
        return Result.success("获取成功", result);
    }
} 