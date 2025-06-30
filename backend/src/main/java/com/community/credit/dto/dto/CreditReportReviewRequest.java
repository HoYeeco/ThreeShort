package com.community.credit.dto;

import com.community.credit.entity.CreditReport;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 信用行为上报审核请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "CreditReportReviewRequest", description = "信用行为上报审核请求")
public class CreditReportReviewRequest {

    @Schema(description = "审核员ID", required = true)
    @NotNull(message = "审核员ID不能为空")
    private Integer reviewerId;

    @Schema(description = "审核状态", required = true)
    @NotNull(message = "审核状态不能为空")
    private CreditReport.ReportStatus status;

    @Schema(description = "审核意见", example = "上报内容属实，符合志愿服务标准")
    @Size(max = 500, message = "审核意见长度不能超过500个字符")
    private String reviewComment;

    @Schema(description = "实际得分", example = "10")
    private Integer scoreAwarded;
} 