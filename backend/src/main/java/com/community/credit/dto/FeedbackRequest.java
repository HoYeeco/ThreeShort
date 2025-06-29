package com.community.credit.dto;

import com.community.credit.entity.FeedbackSuggestion.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 反馈建议提交请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "FeedbackRequest", description = "反馈建议提交请求")
public class FeedbackRequest {

    @Schema(description = "反馈类型", required = true)
    @NotNull(message = "反馈类型不能为空")
    private FeedbackType type;

    @Schema(description = "反馈标题", required = true)
    @NotBlank(message = "反馈标题不能为空")
    @Size(max = 200, message = "反馈标题长度不能超过200个字符")
    private String title;

    @Schema(description = "反馈内容", required = true)
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 2000, message = "反馈内容长度不能超过2000个字符")
    private String content;

    @Schema(description = "附件文件ID列表")
    @Size(max = 5, message = "附件文件不能超过5个")
    private List<Integer> attachmentFileIds;
} 