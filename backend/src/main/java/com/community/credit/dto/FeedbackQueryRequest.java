package com.community.credit.dto;

import com.community.credit.entity.FeedbackSuggestion.FeedbackStatus;
import com.community.credit.entity.FeedbackSuggestion.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 反馈建议查询请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "FeedbackQueryRequest", description = "反馈建议查询请求")
public class FeedbackQueryRequest {

    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "关键词（标题/内容）")
    private String keyword;

    @Schema(description = "反馈类型")
    private FeedbackType type;

    @Schema(description = "处理状态")
    private FeedbackStatus status;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "处理人ID")
    private Integer handlerId;
} 