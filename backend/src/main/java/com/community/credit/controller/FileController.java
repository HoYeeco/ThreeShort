package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.dto.FileUploadResponse;
import com.community.credit.service.FileManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 文件管理控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "文件管理", description = "文件上传、下载等接口")
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileManagementService fileManagementService;

    @Value("${app.upload-path:/uploads/}")
    private String uploadPath;

    @Operation(summary = "上传单个文件", description = "上传单个文件，支持图片类型")
    @PostMapping("/upload")
    public Result<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "businessType", defaultValue = "COMMON") String businessType,
                                               @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        FileUploadResponse response = fileManagementService.uploadFile(file, userId, businessType);
        return Result.success(response);
    }

    @Operation(summary = "批量上传文件", description = "批量上传文件，支持图片类型")
    @PostMapping("/upload/batch")
    public Result<List<FileUploadResponse>> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                                       @RequestParam(value = "businessType", defaultValue = "COMMON") String businessType,
                                                       @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        List<FileUploadResponse> responses = fileManagementService.uploadFiles(files, userId, businessType);
        return Result.success(responses);
    }

    @Operation(summary = "文件下载", description = "根据文件路径下载文件")
    @GetMapping("/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String year,
                                               @PathVariable String month,
                                               @PathVariable String day,
                                               @PathVariable String filename) {
        try {
            String filePath = uploadPath + File.separator + year + File.separator + month + File.separator + day + File.separator + filename;
            File file = new File(filePath);
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            
            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "删除文件", description = "删除指定的文件")
    @DeleteMapping("/{fileId}")
    public Result<String> deleteFile(@PathVariable Integer fileId,
                                    @RequestParam(value = "userId", defaultValue = "1") Integer userId) {
        fileManagementService.deleteFile(fileId, userId);
        return Result.success("文件删除成功");
    }

    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/avi";
            case "mov":
                return "video/mov";
            case "wmv":
                return "video/wmv";
            case "flv":
                return "video/flv";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }
} 