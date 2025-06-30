package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文件管理实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("file_management")
@Schema(name = "FileManagement", description = "文件管理")
public class FileManagement {

    @Schema(description = "文件ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "原始文件名")
    @TableField("original_name")
    private String originalName;

    @Schema(description = "存储文件名")
    @TableField("stored_name")
    private String storedName;

    @Schema(description = "文件路径")
    @TableField("file_path")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    @TableField("file_size")
    private Long fileSize;

    @Schema(description = "文件类型")
    @TableField("file_type")
    private String fileType;

    @Schema(description = "MIME类型")
    @TableField("mime_type")
    private String mimeType;

    @Schema(description = "上传用户ID")
    @TableField("upload_user_id")
    private Integer uploadUserId;

    @Schema(description = "业务类型")
    @TableField("business_type")
    private String businessType;

    @Schema(description = "关联业务ID")
    @TableField("business_id")
    private Integer businessId;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
} 