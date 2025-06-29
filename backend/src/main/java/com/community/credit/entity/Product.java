package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("products")
@Schema(name = "Product", description = "商品信息")
public class Product {

    @Schema(description = "商品ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "商品名称")
    @TableField("name")
    private String name;

    @Schema(description = "商品描述")
    @TableField("description")
    private String description;

    @Schema(description = "所需积分")
    @TableField("points_required")
    private Integer pointsRequired;

    @Schema(description = "库存数量")
    @TableField("stock_quantity")
    private Integer stockQuantity;

    @Schema(description = "商品分类")
    @TableField("category")
    private String category;

    @Schema(description = "商品图片")
    @TableField("image_url")
    private String imageUrl;

    @Schema(description = "可兑换信用等级")
    @TableField("eligible_levels")
    private List<String> eligibleLevels;

    @Schema(description = "是否启用")
    @TableField("is_active")
    private Boolean isActive;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
} 