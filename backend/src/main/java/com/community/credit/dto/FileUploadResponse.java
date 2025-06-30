package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "FileUploadResponse", description = "文件上传响应")
public class FileUploadResponse {

    @Schema(description = "文件ID")
    private Integer fileId;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;
} 