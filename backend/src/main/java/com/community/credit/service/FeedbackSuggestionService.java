package com.community.credit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.FeedbackHandleRequest;
import com.community.credit.dto.FeedbackQueryRequest;
import com.community.credit.dto.FeedbackRequest;
import com.community.credit.entity.FeedbackSuggestion;

/**
 * 反馈建议Service接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface FeedbackSuggestionService extends IService<FeedbackSuggestion> {

    /**
     * 提交反馈建议
     *
     * @param request 反馈建议请求
     * @param userId 提交用户ID
     * @return 反馈建议ID
     */
    Integer submitFeedback(FeedbackRequest request, Integer userId);

    /**
     * 分页查询反馈建议
     *
     * @param request 查询请求
     * @param currentUserId 当前用户ID
     * @return 分页结果
     */
    IPage<FeedbackSuggestion> queryFeedbacks(FeedbackQueryRequest request, Integer currentUserId);

    /**
     * 获取反馈建议详情
     *
     * @param feedbackId 反馈建议ID
     * @param currentUserId 当前用户ID
     * @return 反馈建议详情
     */
    FeedbackSuggestion getFeedbackDetail(Integer feedbackId, Integer currentUserId);

    /**
     * 处理反馈建议
     *
     * @param feedbackId 反馈建议ID
     * @param request 处理请求
     * @param handlerId 处理人ID
     */
    void handleFeedback(Integer feedbackId, FeedbackHandleRequest request, Integer handlerId);

    /**
     * 检查用户是否有权限访问反馈建议
     *
     * @param feedbackId 反馈建议ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean checkFeedbackPermission(Integer feedbackId, Integer userId);
} 