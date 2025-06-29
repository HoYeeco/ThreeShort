package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.FileUploadResponse;
import com.community.credit.entity.FileManagement;
import com.community.credit.mapper.FileManagementMapper;
import com.community.credit.service.FileManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

/**
 * 文件管理Service实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class FileManagementServiceImpl extends ServiceImpl<FileManagementMapper, FileManagement> 
        implements FileManagementService {

    @Value("${app.upload-path:./uploads/}")
    private String uploadPath;

    @Value("${app.max-file-size:10}")
    private Long maxFileSizeMB;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            // 图片类型
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp",
            // 视频类型
            "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv",
            // 文档类型
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, Integer userId, String businessType) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 检查文件大小
        long maxFileSize = maxFileSizeMB * 1024 * 1024; // 转换为字节
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小不能超过" + maxFileSizeMB + "MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new RuntimeException("不支持的文件类型，支持的类型：图片、视频、文档");
        }

        try {
            // 创建上传目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fullUploadPath = uploadPath + datePath;
            Path uploadDir = Paths.get(fullUploadPath);
            
            log.info("创建上传目录: {}", fullUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("目录创建成功: {}", fullUploadPath);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedFilename = UUID.randomUUID().toString() + fileExtension;
            String filePath = fullUploadPath + File.separator + storedFilename;

            log.info("保存文件到: {}", filePath);
            // 保存文件
            file.transferTo(new File(filePath));
            log.info("文件保存成功: {}", filePath);

            // 创建文件记录
            FileManagement fileManagement = new FileManagement();
            fileManagement.setOriginalName(originalFilename);
            fileManagement.setStoredName(storedFilename);
            fileManagement.setFilePath(datePath + "/" + storedFilename);
            fileManagement.setFileSize(file.getSize());
            fileManagement.setFileType(getFileType(originalFilename));
            fileManagement.setMimeType(contentType);
            fileManagement.setUploadUserId(userId);
            fileManagement.setBusinessType(businessType);

            save(fileManagement);

            // 生成访问URL
            String fileUrl = generateFileUrl(fileManagement.getFilePath());

            return new FileUploadResponse(
                    fileManagement.getId(),
                    originalFilename,
                    fileUrl,
                    file.getSize(),
                    fileManagement.getFileType()
            );

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<FileUploadResponse> uploadFiles(List<MultipartFile> files, Integer userId, String businessType) {
        List<FileUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadFile(file, userId, businessType));
        }
        return responses;
    }

    @Override
    public String getFileUrl(Integer fileId) {
        FileManagement fileManagement = getById(fileId);
        if (fileManagement == null) {
            return null;
        }
        return generateFileUrl(fileManagement.getFilePath());
    }

    @Override
    public List<String> getFileUrls(List<Integer> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<FileManagement> files = listByIds(fileIds);
        List<String> urls = new ArrayList<>();
        for (FileManagement file : files) {
            urls.add(generateFileUrl(file.getFilePath()));
        }
        return urls;
    }

    @Override
    public void deleteFile(Integer fileId, Integer userId) {
        FileManagement fileManagement = getById(fileId);
        if (fileManagement == null) {
            throw new RuntimeException("文件不存在");
        }

        // 检查权限（只有上传者或管理员可以删除）
        if (!fileManagement.getUploadUserId().equals(userId)) {
            throw new RuntimeException("没有权限删除此文件");
        }

        try {
            // 删除物理文件
            String fullPath = uploadPath + File.separator + fileManagement.getFilePath();
            Files.deleteIfExists(Paths.get(fullPath));

            // 删除数据库记录
            removeById(fileId);

        } catch (IOException e) {
            log.error("删除文件失败", e);
            throw new RuntimeException("删除文件失败：" + e.getMessage());
        }
    }

    @Override
    public void associateFilesToBusiness(List<Integer> fileIds, String businessType, Integer businessId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        List<FileManagement> files = listByIds(fileIds);
        for (FileManagement file : files) {
            file.setBusinessType(businessType);
            file.setBusinessId(businessId);
        }
        updateBatchById(files);
    }

    private String getFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateFileUrl(String filePath) {
        return "http://localhost:" + serverPort + contextPath + "/files/" + filePath;
    }
} 