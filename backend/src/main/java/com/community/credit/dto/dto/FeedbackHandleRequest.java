package com.community.credit.dto;

import com.community.credit.entity.FeedbackSuggestion.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 反馈建议处理请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "FeedbackHandleRequest", description = "反馈建议处理请求")
public class FeedbackHandleRequest {

    @Schema(description = "处理状态", required = true)
    @NotNull(message = "处理状态不能为空")
    private FeedbackStatus status;

    @Schema(description = "处理回复", required = true)
    @NotBlank(message = "处理回复不能为空")
    @Size(max = 1000, message = "处理回复长度不能超过1000个字符")
    private String handlerReply;
} 