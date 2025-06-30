package com.community.credit.dto;

import com.community.credit.entity.PointExchangeRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 兑换记录查询请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "ExchangeRecordQueryRequest", description = "兑换记录查询请求参数")
public class ExchangeRecordQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称（模糊搜索）")
    private String productName;

    @Schema(description = "兑换状态")
    private PointExchangeRecord.ExchangeStatus status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "最小积分")
    private Integer minPoints;

    @Schema(description = "最大积分")
    private Integer maxPoints;

    @Schema(description = "排序字段")
    private String sortField = "exchange_time";

    @Schema(description = "排序方向")
    private String sortOrder = "desc";
} 