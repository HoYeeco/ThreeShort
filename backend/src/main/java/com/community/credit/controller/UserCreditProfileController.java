package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.entity.UserCreditProfile;
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
    public Result<UserCreditProfile> getUserProfile(@PathVariable Integer userId) {
        UserCreditProfile profile = userCreditProfileService.getUserProfile(userId);
        if (profile == null) {
            return Result.error(404, "用户信用档案不存在");
        }
        return Result.success("获取成功", profile);
    }

    @Operation(summary = "获取当前用户信用档案", description = "获取当前登录用户的信用档案")
    @GetMapping("/current")
    public Result<UserCreditProfile> getCurrentUserProfile(@RequestParam Integer userId) {
        UserCreditProfile profile = userCreditProfileService.getOrCreateUserProfile(userId);
        return Result.success("获取成功", profile);
    }

    @Operation(summary = "获取用户信用统计", description = "获取用户的信用统计数据")
    @GetMapping("/{userId}/stats")
    public Result<Map<String, Object>> getUserCreditStats(@PathVariable Integer userId) {
        Map<String, Object> stats = userCreditProfileService.getUserCreditStats(userId);
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "获取信用等级分布", description = "获取所有用户的信用等级分布统计")
    @GetMapping("/level-distribution")
    public Result<Map<String, Object>> getCreditLevelDistribution() {
        Map<String, Object> distribution = userCreditProfileService.getCreditLevelDistribution();
        return Result.success("获取成功", distribution);
    }

    @Operation(summary = "获取信用排行榜", description = "获取信用分数排行榜")
    @GetMapping("/ranking")
    public Result<List<UserCreditProfile>> getCreditRanking(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<UserCreditProfile> ranking = userCreditProfileService.getCreditRanking(limit);
        return Result.success("获取成功", ranking);
    }

    @Operation(summary = "重新计算用户信用", description = "重新计算指定用户的信用分数")
    @PostMapping("/{userId}/recalculate")
    public Result<String> recalculateUserCredit(@PathVariable Integer userId) {
        userCreditProfileService.recalculateUserCredit(userId);
        return Result.success("重新计算完成");
    }

    @Operation(summary = "生成用户评分记录", description = "为指定用户生成周期评分记录")
    @PostMapping("/{userId}/score-record")
    public Result<String> generateScoreRecord(@PathVariable Integer userId,
                                            @RequestParam String period) {
        userCreditProfileService.generateScoreRecord(userId, period);
        return Result.success("评分记录生成完成");
    }

    @Operation(summary = "批量生成周期评分", description = "为所有用户生成周期评分记录")
    @PostMapping("/batch-generate-score")
    public Result<String> batchGenerateScoreRecord(@RequestParam String period) {
        userCreditProfileService.batchGenerateScoreRecord(period);
        return Result.success("批量评分记录生成完成");
    }

    @Operation(summary = "获取用户积分历史", description = "获取用户的积分变化历史")
    @GetMapping("/{userId}/score-history")
    public Result<List<Map<String, Object>>> getUserScoreHistory(@PathVariable Integer userId) {
        List<Map<String, Object>> history = userCreditProfileService.getUserScoreHistory(userId);
        return Result.success("获取成功", history);
    }
} 