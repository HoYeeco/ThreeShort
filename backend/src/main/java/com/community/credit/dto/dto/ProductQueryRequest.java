package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品查询请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ProductQueryRequest", description = "商品查询请求参数")
public class ProductQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "商品名称（模糊搜索）")
    private String name;

    @Schema(description = "商品分类")
    private String category;

    @Schema(description = "最小积分")
    private Integer minPoints;

    @Schema(description = "最大积分")
    private Integer maxPoints;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "是否有库存")
    private Boolean hasStock;

    @Schema(description = "用户信用等级（用于筛选可兑换商品）")
    private String userLevel;

    @Schema(description = "排序字段")
    private String sortField = "created_time";

    @Schema(description = "排序方向")
    private String sortOrder = "desc";
} 