package com.community.credit.dto;

import com.community.credit.entity.CommunityAgreement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 公约创建/更新请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "AgreementRequest", description = "公约创建/更新请求参数")
public class AgreementRequest {

    @Schema(description = "公约标题")
    @NotBlank(message = "公约标题不能为空")
    @Size(max = 200, message = "公约标题长度不能超过200个字符")
    private String title;

    @Schema(description = "公约内容")
    @NotBlank(message = "公约内容不能为空")
    private String content;

    @Schema(description = "内容类型")
    @NotNull(message = "内容类型不能为空")
    private CommunityAgreement.ContentType contentType;

    @Schema(description = "视频文件路径")
    @Size(max = 255, message = "视频文件路径长度不能超过255个字符")
    private String videoPath;

    @Schema(description = "排序号")
    @Min(value = 0, message = "排序号不能为负数")
    private Integer orderNum = 0;

    @Schema(description = "是否启用")
    private Boolean isActive = true;
} 