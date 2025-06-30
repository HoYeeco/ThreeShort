package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 积分兑换请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "ExchangeRequest", description = "积分兑换请求参数")
public class ExchangeRequest {

    @Schema(description = "用户ID", example = "1")
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @Schema(description = "商品ID", example = "1")
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    @Schema(description = "兑换数量")
    @NotNull(message = "兑换数量不能为空")
    @Min(value = 1, message = "兑换数量必须大于0")
    private Integer quantity = 1;

    @Schema(description = "备注信息")
    private String remarks;
} 