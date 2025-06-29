package com.community.credit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.credit.common.Result;
import com.community.credit.dto.FeedbackHandleRequest;
import com.community.credit.dto.FeedbackQueryRequest;
import com.community.credit.dto.FeedbackRequest;
import com.community.credit.entity.FeedbackSuggestion;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.FeedbackSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 反馈建议控制器 - 简化版（无JWT认证）
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "反馈建议管理", description = "反馈建议提交、查询、处理等接口")
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackSuggestionService feedbackSuggestionService;

    @PostMapping("/submit")
    @Operation(summary = "提交反馈建议")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Integer> submitFeedback(@Valid @RequestBody FeedbackRequest request,
                                        @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        Integer feedbackId = feedbackSuggestionService.submitFeedback(request, userId);
        return Result.success("提交成功", feedbackId);
    }

    @PostMapping
    @Operation(summary = "提交反馈建议（简化接口）")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Integer> submitFeedbackSimple(@Valid @RequestBody FeedbackRequest request,
                                               @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        Integer feedbackId = feedbackSuggestionService.submitFeedback(request, userId);
        return Result.success("提交成功", feedbackId);
    }

    @GetMapping("/list")
    @Operation(summary = "查询反馈建议列表")
    @RequireRole({User.UserRole.ADMIN})
    public Result<IPage<FeedbackSuggestion>> queryFeedbacks(@Valid FeedbackQueryRequest request,
                                                          @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        IPage<FeedbackSuggestion> result = feedbackSuggestionService.queryFeedbacks(request, userId);
        return Result.success("查询成功", result);
    }

    @GetMapping("/{feedbackId}")
    @Operation(summary = "获取反馈建议详情")
    @RequireRole(value = {User.UserRole.ADMIN}, allowSelf = true)
    public Result<FeedbackSuggestion> getFeedbackDetail(@PathVariable Integer feedbackId,
                                                       @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        FeedbackSuggestion feedback = feedbackSuggestionService.getFeedbackDetail(feedbackId, userId);
        return Result.success("获取成功", feedback);
    }

    @PutMapping("/{feedbackId}/handle")
    @Operation(summary = "处理反馈建议")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> handleFeedback(@PathVariable Integer feedbackId,
                                       @Valid @RequestBody FeedbackHandleRequest request,
                                       @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        feedbackSuggestionService.handleFeedback(feedbackId, request, userId);
        return Result.success("处理成功");
    }

    @GetMapping("/my")
    @Operation(summary = "我的反馈建议")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<IPage<FeedbackSuggestion>> getMyFeedbacks(@Valid FeedbackQueryRequest request,
                                                           @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        // 设置为指定用户
        request.setUserId(userId);
        IPage<FeedbackSuggestion> result = feedbackSuggestionService.queryFeedbacks(request, userId);
        return Result.success("查询成功", result);
    }

    @GetMapping("/{feedbackId}/permission")
    @Operation(summary = "检查反馈权限")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Boolean> checkFeedbackPermission(@PathVariable Integer feedbackId,
                                                  @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        boolean hasPermission = feedbackSuggestionService.checkFeedbackPermission(feedbackId, userId);
        return Result.success("检查完成", hasPermission);
    }
} 