package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.FeedbackHandleRequest;
import com.community.credit.dto.FeedbackQueryRequest;
import com.community.credit.dto.FeedbackRequest;
import com.community.credit.entity.FeedbackSuggestion;
import com.community.credit.entity.User;
import com.community.credit.mapper.FeedbackSuggestionMapper;
import com.community.credit.service.FeedbackSuggestionService;
import com.community.credit.service.FileManagementService;
import com.community.credit.service.SystemLogService;
import com.community.credit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 反馈建议Service实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class FeedbackSuggestionServiceImpl extends ServiceImpl<FeedbackSuggestionMapper, FeedbackSuggestion>
        implements FeedbackSuggestionService {

    @Autowired
    private FileManagementService fileManagementService;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Integer submitFeedback(FeedbackRequest request, Integer userId) {
        // 创建反馈建议
        FeedbackSuggestion feedback = new FeedbackSuggestion();
        feedback.setUserId(userId);
        feedback.setType(request.getType());
        feedback.setTitle(request.getTitle());
        feedback.setContent(request.getContent());
        feedback.setStatus(FeedbackSuggestion.FeedbackStatus.PENDING);

        // 处理附件文件
        if (request.getAttachmentFileIds() != null && !request.getAttachmentFileIds().isEmpty()) {
            List<String> fileUrls = fileManagementService.getFileUrls(request.getAttachmentFileIds());
            try {
                String attachmentFilesJson = objectMapper.writeValueAsString(fileUrls);
                feedback.setAttachmentFiles(attachmentFilesJson);
            } catch (JsonProcessingException e) {
                log.error("转换附件文件列表为JSON失败", e);
                feedback.setAttachmentFiles("[]");
            }
        } else {
            feedback.setAttachmentFiles("[]");
        }

        save(feedback);

        // 关联附件文件到业务
        if (request.getAttachmentFileIds() != null && !request.getAttachmentFileIds().isEmpty()) {
            fileManagementService.associateFilesToBusiness(
                    request.getAttachmentFileIds(), 
                    "FEEDBACK", 
                    feedback.getId()
            );
        }

        // 记录系统日志
        systemLogService.recordSuccessLog(userId, "FEEDBACK_SUBMIT", 
                "提交反馈建议：" + request.getTitle(), 
                "POST", "/feedback", null, "提交成功");

        log.info("用户 {} 提交反馈建议：{}", userId, request.getTitle());

        return feedback.getId();
    }

    /**
     * 将JSON字符串转换为文件URL列表
     */
    private List<String> parseAttachmentFiles(String attachmentFiles) {
        if (!StringUtils.hasText(attachmentFiles)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(attachmentFiles, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析附件文件JSON失败: {}", attachmentFiles, e);
            return new ArrayList<>();
        }
    }

    @Override
    public IPage<FeedbackSuggestion> queryFeedbacks(FeedbackQueryRequest request, Integer currentUserId) {
        Page<FeedbackSuggestion> page = new Page<>(request.getPage(), request.getSize());

        LambdaQueryWrapper<FeedbackSuggestion> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(FeedbackSuggestion::getTitle, request.getKeyword())
                    .or()
                    .like(FeedbackSuggestion::getContent, request.getKeyword())
            );
        }

        // 类型筛选
        if (request.getType() != null) {
            queryWrapper.eq(FeedbackSuggestion::getType, request.getType());
        }

        // 状态筛选
        if (request.getStatus() != null) {
            queryWrapper.eq(FeedbackSuggestion::getStatus, request.getStatus());
        }

        // 权限控制
        User currentUser = userService.getById(currentUserId);
        if (currentUser.getRole() == User.UserRole.RESIDENT) {
            // 普通居民只能查看自己的反馈
            queryWrapper.eq(FeedbackSuggestion::getUserId, currentUserId);
        } else {
            // 管理员可以查看所有反馈
            if (request.getUserId() != null) {
                queryWrapper.eq(FeedbackSuggestion::getUserId, request.getUserId());
            }
            if (request.getHandlerId() != null) {
                queryWrapper.eq(FeedbackSuggestion::getHandlerId, request.getHandlerId());
            }
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(FeedbackSuggestion::getCreatedTime);

        IPage<FeedbackSuggestion> result = page(page, queryWrapper);
        
        // 处理附件文件JSON转换
        result.getRecords().forEach(feedback -> {
            feedback.setAttachmentFileList(parseAttachmentFiles(feedback.getAttachmentFiles()));
        });

        return result;
    }

    @Override
    public FeedbackSuggestion getFeedbackDetail(Integer feedbackId, Integer currentUserId) {
        FeedbackSuggestion feedback = getById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈建议不存在");
        }

        // 权限检查
        if (!checkFeedbackPermission(feedbackId, currentUserId)) {
            throw new RuntimeException("没有权限查看此反馈建议");
        }

        // 处理附件文件JSON转换
        feedback.setAttachmentFileList(parseAttachmentFiles(feedback.getAttachmentFiles()));

        return feedback;
    }

    @Override
    @Transactional
    public void handleFeedback(Integer feedbackId, FeedbackHandleRequest request, Integer handlerId) {
        FeedbackSuggestion feedback = getById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈建议不存在");
        }

        if (feedback.getStatus() == FeedbackSuggestion.FeedbackStatus.CLOSED) {
            throw new RuntimeException("反馈建议已关闭，无法处理");
        }

        // 更新反馈状态
        feedback.setStatus(request.getStatus());
        feedback.setHandlerId(handlerId);
        feedback.setHandlerReply(request.getHandlerReply());
        feedback.setHandleTime(LocalDateTime.now());

        updateById(feedback);

        // 记录系统日志
        systemLogService.recordSuccessLog(handlerId, "FEEDBACK_HANDLE", 
                "处理反馈建议：" + feedback.getTitle() + "，状态：" + request.getStatus(), 
                "PUT", "/feedback/" + feedbackId + "/handle", null, "处理成功");

        log.info("管理员 {} 处理反馈建议 {}，状态：{}", handlerId, feedbackId, request.getStatus());
    }

    @Override
    public boolean checkFeedbackPermission(Integer feedbackId, Integer userId) {
        if (feedbackId == null || userId == null) {
            return false;
        }

        FeedbackSuggestion feedback = getById(feedbackId);
        if (feedback == null) {
            return false;
        }

        User user = userService.getById(userId);
        if (user == null) {
            return false;
        }

        // 管理员和维护员可以查看所有反馈
        if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MAINTAINER) {
            return true;
        }

        // 普通用户只能查看自己的反馈
        return feedback.getUserId().equals(userId);
    }
} 