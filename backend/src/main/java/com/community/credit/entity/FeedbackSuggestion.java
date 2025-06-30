package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 反馈建议实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("feedback_suggestions")
@Schema(name = "FeedbackSuggestion", description = "反馈建议")
public class FeedbackSuggestion {

    @Schema(description = "反馈ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "提交用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "反馈类型")
    @TableField("type")
    private FeedbackType type;

    @Schema(description = "反馈标题")
    @TableField("title")
    private String title;

    @Schema(description = "反馈内容")
    @TableField("content")
    private String content;

    @Schema(description = "附件文件JSON字符串")
    @TableField("attachment_files")
    private String attachmentFiles;

    @Schema(description = "附件文件列表")
    @TableField(exist = false)
    private List<String> attachmentFileList;

    @Schema(description = "处理状态")
    @TableField("status")
    private FeedbackStatus status;

    @Schema(description = "处理人ID")
    @TableField("handler_id")
    private Integer handlerId;

    @Schema(description = "处理回复")
    @TableField("handler_reply")
    private String handlerReply;

    @Schema(description = "处理时间")
    @TableField("handle_time")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 反馈类型枚举
     */
    public enum FeedbackType {
        SUGGESTION, COMPLAINT, LEARNING_FEEDBACK, OTHER
    }

    /**
     * 反馈状态枚举
     */
    public enum FeedbackStatus {
        PENDING, PROCESSING, RESOLVED, CLOSED
    }
} 