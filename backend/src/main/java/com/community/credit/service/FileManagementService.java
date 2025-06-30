package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.FileUploadResponse;
import com.community.credit.entity.FileManagement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理Service接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface FileManagementService extends IService<FileManagement> {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param userId 上传用户ID
     * @param businessType 业务类型
     * @return 文件上传响应
     */
    FileUploadResponse uploadFile(MultipartFile file, Integer userId, String businessType);

    /**
     * 批量上传文件
     *
     * @param files 文件列表
     * @param userId 上传用户ID
     * @param businessType 业务类型
     * @return 文件上传响应列表
     */
    List<FileUploadResponse> uploadFiles(List<MultipartFile> files, Integer userId, String businessType);

    /**
     * 获取文件访问URL
     *
     * @param fileId 文件ID
     * @return 文件访问URL
     */
    String getFileUrl(Integer fileId);

    /**
     * 根据文件ID列表获取文件URL列表
     *
     * @param fileIds 文件ID列表
     * @return 文件URL列表
     */
    List<String> getFileUrls(List<Integer> fileIds);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 删除用户ID
     */
    void deleteFile(Integer fileId, Integer userId);

    /**
     * 关联文件到业务
     *
     * @param fileIds 文件ID列表
     * @param businessType 业务类型
     * @param businessId 业务ID
     */
    void associateFilesToBusiness(List<Integer> fileIds, String businessType, Integer businessId);
} 