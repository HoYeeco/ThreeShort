package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户信用档案实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_credit_profiles")
@Schema(name = "UserCreditProfile", description = "用户信用档案")
public class UserCreditProfile {

    @Schema(description = "档案ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "当前信用分")
    @TableField("current_score")
    private Integer currentScore;

    @Schema(description = "奖励积分")
    @TableField("reward_points")
    private Integer rewardPoints;

    @Schema(description = "信用等级")
    @TableField("credit_level")
    private String creditLevel;

    @Schema(description = "总上报次数")
    @TableField("total_reports")
    private Integer totalReports;

    @Schema(description = "通过审核次数")
    @TableField("approved_reports")
    private Integer approvedReports;

    @Schema(description = "最后积分更新时间")
    @TableField("last_score_update")
    private LocalDateTime lastScoreUpdate;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @Schema(description = "用户真实姓名")
    @TableField(exist = false)
    private String realName;
} 