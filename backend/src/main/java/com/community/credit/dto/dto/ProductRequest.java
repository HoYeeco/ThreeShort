package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 商品请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "ProductRequest", description = "商品请求参数")
public class ProductRequest {

    @Schema(description = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "所需积分")
    @NotNull(message = "所需积分不能为空")
    @Min(value = 1, message = "所需积分必须大于0")
    private Integer pointsRequired;

    @Schema(description = "库存数量")
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    private Integer stockQuantity;

    @Schema(description = "商品分类")
    private String category;

    @Schema(description = "商品图片URL")
    private String imageUrl;

    @Schema(description = "可兑换信用等级（兼容旧版本）")
    private List<String> eligibleLevels;

    @Schema(description = "最低兑换等级")
    private String minEligibleLevel;

    @Schema(description = "是否启用")
    private Boolean isActive = true;
} 