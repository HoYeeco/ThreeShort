package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 信用行为类型实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_behavior_types")
@Schema(name = "CreditBehaviorType", description = "信用行为类型")
public class CreditBehaviorType {

    @Schema(description = "行为类型ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "行为类型名称")
    @TableField("name")
    private String name;

    @Schema(description = "行为分类：CREDIT守信/DISCREDIT失信")
    @TableField("category")
    private BehaviorCategory category;

    @Schema(description = "评分规则（正数加分，负数扣分）")
    @TableField("score_rule")
    private Integer scoreRule;

    @Schema(description = "行为描述")
    @TableField("description")
    private String description;

    @Schema(description = "是否启用")
    @TableField("is_active")
    private Boolean isActive;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 行为分类枚举
     */
    public enum BehaviorCategory {
        CREDIT, DISCREDIT
    }
} 