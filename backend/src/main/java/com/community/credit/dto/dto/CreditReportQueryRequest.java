package com.community.credit.dto;

import com.community.credit.entity.CreditReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 信用行为上报查询请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "CreditReportQueryRequest", description = "信用行为上报查询请求")
public class CreditReportQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer current = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "行为类型ID")
    private Integer behaviorTypeId;

    @Schema(description = "审核状态")
    private CreditReport.ReportStatus status;

    @Schema(description = "审核员ID")
    private Integer reviewerId;

    @Schema(description = "搜索关键词（标题/描述）")
    private String keyword;

    @Schema(description = "开始时间", example = "2024-01-01")
    private String startTime;

    @Schema(description = "结束时间", example = "2024-12-31")
    private String endTime;
} 