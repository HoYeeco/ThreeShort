package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 公约AI问答请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "AgreementQuestionRequest", description = "公约AI问答请求参数")
public class AgreementQuestionRequest {

    @Schema(description = "用户问题")
    @NotBlank(message = "问题内容不能为空")
    @Size(max = 500, message = "问题内容长度不能超过500个字符")
    private String question;

    @Schema(description = "公约ID（可选，指定特定公约）")
    private Integer agreementId;

    @Schema(description = "会话ID（可选，用于上下文对话）")
    private String sessionId;
} 