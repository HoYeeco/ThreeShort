package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 信用评分记录实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_score_records")
@Schema(name = "CreditScoreRecord", description = "信用评分记录")
public class CreditScoreRecord {

    @Schema(description = "记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "评分周期")
    @TableField("score_period")
    private String scorePeriod;

    @Schema(description = "周期得分")
    @TableField("period_score")
    private Integer periodScore;

    @Schema(description = "获得奖励积分")
    @TableField("reward_points_gained")
    private Integer rewardPointsGained;

    @Schema(description = "计算时间")
    @TableField("calculation_time")
    private LocalDateTime calculationTime;

    @Schema(description = "详细计算过程JSON")
    @TableField("details")
    private String details;
} 