package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 信用行为上报实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("credit_reports")
@Schema(name = "CreditReport", description = "信用行为上报")
public class CreditReport {

    @Schema(description = "上报ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "上报用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "行为类型ID")
    @TableField("behavior_type_id")
    private Integer behaviorTypeId;

    @Schema(description = "行为类型名称")
    @TableField(exist = false)
    private String behaviorType;

    @Schema(description = "上报标题")
    @TableField("title")
    private String title;

    @Schema(description = "行为描述")
    @TableField("description")
    private String description;

    @Schema(description = "证据文件（图片/视频）")
    @TableField(value = "evidence_files", typeHandler = JacksonTypeHandler.class)
    private List<String> evidenceFiles;

    @Schema(description = "发生地点")
    @TableField("location")
    private String location;

    @Schema(description = "涉及人员")
    @TableField("involved_persons")
    private String involvedPersons;

    @Schema(description = "被申报人ID")
    @TableField("reported_user_id")
    private Integer reportedUserId;

    @Schema(description = "上报时间")
    @TableField("report_time")
    private LocalDateTime reportTime;

    @Schema(description = "审核状态")
    @TableField("status")
    private ReportStatus status;

    @Schema(description = "审核员ID")
    @TableField("reviewer_id")
    private Integer reviewerId;

    @Schema(description = "审核时间")
    @TableField("review_time")
    private LocalDateTime reviewTime;

    @Schema(description = "审核意见")
    @TableField("review_comment")
    private String reviewComment;

    @Schema(description = "获得分数")
    @TableField("score_awarded")
    private Integer scoreAwarded;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 上报状态枚举
     */
    public enum ReportStatus {
        PENDING, APPROVED, REJECTED, WITHDRAWN
    }
} 