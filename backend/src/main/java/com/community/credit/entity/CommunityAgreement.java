package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社区公约实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("community_agreements")
@Schema(name = "CommunityAgreement", description = "社区公约")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityAgreement {

    @Schema(description = "公约ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "公约标题")
    @TableField("title")
    private String title;

    @Schema(description = "公约内容")
    @TableField("content")
    private String content;

    @Schema(description = "内容类型")
    @TableField("content_type")
    private ContentType contentType;

    @Schema(description = "视频文件路径")
    @TableField("video_path")
    private String videoPath;

    @Schema(description = "排序号")
    @TableField("order_num")
    private Integer orderNum;

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
     * 内容类型枚举
     */
    public enum ContentType {
        TEXT("TEXT", "文字"),
        VIDEO("VIDEO", "视频"),
        MIXED("MIXED", "图文混合");

        private final String code;
        private final String description;

        ContentType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
} 