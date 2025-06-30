package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.CreditReportQueryRequest;
import com.community.credit.dto.CreditReportRequest;
import com.community.credit.dto.CreditReportReviewRequest;
import com.community.credit.entity.CreditBehaviorType;
import com.community.credit.entity.CreditReport;
import com.community.credit.entity.UserCreditProfile;
import com.community.credit.mapper.CreditReportMapper;
import com.community.credit.service.CreditBehaviorTypeService;
import com.community.credit.service.CreditReportService;
import com.community.credit.service.SystemLogService;
import com.community.credit.service.UserCreditProfileService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 信用行为上报服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CreditReportServiceImpl extends ServiceImpl<CreditReportMapper, CreditReport> implements CreditReportService {

    @Autowired
    private CreditBehaviorTypeService creditBehaviorTypeService;

    @Autowired
    private UserCreditProfileService userCreditProfileService;

    @Autowired
    private SystemLogService systemLogService;

    @Override
    @Transactional
    public void submitReport(CreditReportRequest request) {
        // 验证行为类型是否存在且启用
        CreditBehaviorType behaviorType = creditBehaviorTypeService.getById(request.getBehaviorTypeId());
        if (behaviorType == null || !behaviorType.getIsActive()) {
            throw new RuntimeException("行为类型不存在或已禁用");
        }

        // 创建上报记录
        CreditReport report = new CreditReport();
        BeanUtils.copyProperties(request, report);
        report.setReportTime(LocalDateTime.now());
        report.setStatus(CreditReport.ReportStatus.PENDING);

        this.save(report);

        // 记录操作日志
        systemLogService.recordSuccessLog(
            request.getUserId(),
            "CREDIT_REPORT",
            String.format("用户提交信用行为上报：%s（%s）", report.getTitle(), behaviorType.getName()),
            "POST",
            "/credit-report/submit",
            String.format("{\"behaviorTypeId\":%d,\"title\":\"%s\"}", request.getBehaviorTypeId(), request.getTitle()),
            String.format("{\"reportId\":%d}", report.getId())
        );
    }

    @Override
    public IPage<CreditReport> queryReports(CreditReportQueryRequest request) {
        Page<Map<String, Object>> page = new Page<>(request.getCurrent(), request.getSize());
        
        // 时间范围处理
        String startTime = null;
        String endTime = null;
        if (StringUtils.hasText(request.getStartTime())) {
            startTime = request.getStartTime() + " 00:00:00";
        }
        if (StringUtils.hasText(request.getEndTime())) {
            endTime = request.getEndTime() + " 23:59:59";
        }
        
        // 使用关联查询方法
        IPage<Map<String, Object>> resultPage = baseMapper.selectReportsWithBehaviorType(
            page,
            request.getUserId(),
            request.getBehaviorTypeId(),
            request.getStatus() != null ? request.getStatus().toString() : null,
            request.getReviewerId(),
            request.getKeyword(),
            startTime,
            endTime
        );
        
        // 转换为CreditReport对象的分页结果
        Page<CreditReport> creditReportPage = new Page<>(request.getCurrent(), request.getSize());
        creditReportPage.setTotal(resultPage.getTotal());
        creditReportPage.setPages(resultPage.getPages());
        
        // 将Map结果转换为CreditReport对象，并设置behaviorType字段
        java.util.List<CreditReport> records = resultPage.getRecords().stream()
            .map(map -> {
                CreditReport report = new CreditReport();
                // 基本字段映射
                if (map.get("id") != null) report.setId((Integer) map.get("id"));
                if (map.get("user_id") != null) report.setUserId((Integer) map.get("user_id"));
                if (map.get("behavior_type_id") != null) report.setBehaviorTypeId((Integer) map.get("behavior_type_id"));
                if (map.get("title") != null) report.setTitle((String) map.get("title"));
                if (map.get("description") != null) report.setDescription((String) map.get("description"));
                if (map.get("evidence_files") != null) {
                    // 处理证据文件字段，从JSON字符串转换为List
                    String evidenceFilesJson = (String) map.get("evidence_files");
                    if (evidenceFilesJson != null && !evidenceFilesJson.trim().isEmpty()) {
                        try {
                            // 使用Jackson将JSON字符串转换为List
                            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            java.util.List<String> evidenceFiles = objectMapper.readValue(evidenceFilesJson, 
                                new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
                            report.setEvidenceFiles(evidenceFiles);
                        } catch (Exception e) {
                            log.warn("解析证据文件JSON失败: {}", evidenceFilesJson, e);
                            report.setEvidenceFiles(new java.util.ArrayList<>());
                        }
                    } else {
                        report.setEvidenceFiles(new java.util.ArrayList<>());
                    }
                }
                if (map.get("location") != null) report.setLocation((String) map.get("location"));
                if (map.get("involved_persons") != null) report.setInvolvedPersons((String) map.get("involved_persons"));
                if (map.get("report_time") != null) report.setReportTime((LocalDateTime) map.get("report_time"));
                if (map.get("status") != null) report.setStatus(CreditReport.ReportStatus.valueOf((String) map.get("status")));
                if (map.get("score_awarded") != null) report.setScoreAwarded((Integer) map.get("score_awarded"));
                if (map.get("reviewer_id") != null) report.setReviewerId((Integer) map.get("reviewer_id"));
                if (map.get("review_time") != null) report.setReviewTime((LocalDateTime) map.get("review_time"));
                if (map.get("review_comment") != null) report.setReviewComment((String) map.get("review_comment"));
                if (map.get("created_time") != null) report.setCreatedTime((LocalDateTime) map.get("created_time"));
                if (map.get("updated_time") != null) report.setUpdatedTime((LocalDateTime) map.get("updated_time"));
                
                // 设置行为类型名称 - 这是新增的字段
                if (map.get("behavior_type") != null) {
                    report.setBehaviorType((String) map.get("behavior_type"));
                }
                
                return report;
            })
            .collect(java.util.stream.Collectors.toList());
            
        creditReportPage.setRecords(records);
        return creditReportPage;
    }

    @Override
    public CreditReport getReportDetail(Integer reportId) {
        return getById(reportId);
    }

    @Override
    @Transactional
    public void reviewReport(Integer reportId, CreditReportReviewRequest request) {
        CreditReport report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("上报记录不存在");
        }

        if (report.getStatus() != CreditReport.ReportStatus.PENDING) {
            throw new RuntimeException("该上报记录已经审核过了");
        }

        // 更新审核信息
        report.setStatus(request.getStatus());
        report.setReviewerId(request.getReviewerId());
        report.setReviewTime(LocalDateTime.now());
        report.setReviewComment(request.getReviewComment());

        if (request.getStatus() == CreditReport.ReportStatus.APPROVED) {
            // 使用审核员指定的分数，如果没有指定则使用行为类型的默认分数
            Integer scoreToAward = request.getScoreAwarded();
            if (scoreToAward == null) {
                // 如果前端没有传入分数，则使用行为类型的默认分数
                CreditBehaviorType behaviorType = creditBehaviorTypeService.getById(report.getBehaviorTypeId());
                if (behaviorType != null) {
                    scoreToAward = behaviorType.getScoreRule();
                } else {
                    scoreToAward = 0; // 默认0分
                }
            }
            
            report.setScoreAwarded(scoreToAward);
            
            // 确定获得分数的用户ID：优先给被申报人，如果没有被申报人则给申报人
            Integer scoreRecipientId = report.getReportedUserId() != null ? 
                report.getReportedUserId() : report.getUserId();
            
            // 更新用户信用分数
            userCreditProfileService.updateCreditScore(scoreRecipientId, 
                scoreToAward, "信用行为上报审核通过");

            
            // 更新用户信用档案的统计字段（申报人的统计）
            updateUserProfileStats(report.getUserId());

            // 记录审核通过日志
            String logMessage = report.getReportedUserId() != null ? 
                String.format("审核员审核通过信用上报：%s，为被申报人(ID:%d)奖励%d分", 
                    report.getTitle(), scoreRecipientId, scoreToAward) :
                String.format("审核员审核通过信用上报：%s，为申报人奖励%d分", 
                    report.getTitle(), scoreToAward);
                    
            systemLogService.recordSuccessLog(
                request.getReviewerId(),
                "CREDIT_REVIEW",
                logMessage,
                "PUT",
                "/credit-report/" + reportId + "/review",
                String.format("{\"status\":\"APPROVED\",\"scoreAwarded\":%d}", scoreToAward),
                String.format("{\"reportId\":%d,\"userId\":%d,\"scoreRecipientId\":%d,\"score\":%d}", 
                    reportId, report.getUserId(), scoreRecipientId, scoreToAward)
            );
        } else if (request.getStatus() == CreditReport.ReportStatus.REJECTED) {
            // 记录审核拒绝日志
            systemLogService.recordSuccessLog(
                request.getReviewerId(),
                "CREDIT_REVIEW",
                String.format("审核员拒绝信用上报：%s，原因：%s", report.getTitle(), request.getReviewComment()),
                "PUT",
                "/credit-report/" + reportId + "/review",
                String.format("{\"status\":\"REJECTED\",\"reviewComment\":\"%s\"}", request.getReviewComment()),
                String.format("{\"reportId\":%d,\"userId\":%d}", reportId, report.getUserId())
            );
        }

        updateById(report);
    }

    @Override
    public void withdrawReport(Integer reportId, Integer userId) {
        CreditReport report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("上报记录不存在");
        }

        if (!report.getUserId().equals(userId)) {
            throw new RuntimeException("无权限撤回该上报记录");
        }

        if (report.getStatus() != CreditReport.ReportStatus.PENDING) {
            throw new RuntimeException("只能撤回待审核的上报记录");
        }

        report.setStatus(CreditReport.ReportStatus.WITHDRAWN);
        updateById(report);
    }

    @Override
    public Long getPendingCount() {
        QueryWrapper<CreditReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", CreditReport.ReportStatus.PENDING);
        return this.count(queryWrapper);
    }

    @Override
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总上报数
        long totalReports = this.count();
        statistics.put("totalReports", totalReports);
        
        // 待审核数量
        QueryWrapper<CreditReport> pendingQuery = new QueryWrapper<>();
        pendingQuery.eq("status", CreditReport.ReportStatus.PENDING);
        long pendingReports = this.count(pendingQuery);
        statistics.put("pendingReports", pendingReports);
        
        // 已通过数量
        QueryWrapper<CreditReport> approvedQuery = new QueryWrapper<>();
        approvedQuery.eq("status", CreditReport.ReportStatus.APPROVED);
        long approvedReports = this.count(approvedQuery);
        statistics.put("approvedReports", approvedReports);
        
        // 已拒绝数量
        QueryWrapper<CreditReport> rejectedQuery = new QueryWrapper<>();
        rejectedQuery.eq("status", CreditReport.ReportStatus.REJECTED);
        long rejectedReports = this.count(rejectedQuery);
        statistics.put("rejectedReports", rejectedReports);
        
        return statistics;
    }

    /**
     * 更新用户信用档案的统计字段
     */
    private void updateUserProfileStats(Integer userId) {
        // 获取用户的上报统计数据
        Map<String, Object> reportStats = baseMapper.getUserReportStats(userId);
        
        Integer totalReports = reportStats.get("total_reports") != null ? 
            ((Number) reportStats.get("total_reports")).intValue() : 0;
        Integer approvedReports = reportStats.get("approved_count") != null ? 
            ((Number) reportStats.get("approved_count")).intValue() : 0;
        
        // 更新用户信用档案的统计字段
        UserCreditProfile profile = userCreditProfileService.getOrCreateUserProfile(userId);
        profile.setTotalReports(totalReports);
        profile.setApprovedReports(approvedReports);
        
        userCreditProfileService.updateById(profile);
        
        // 清除缓存，确保数据一致性
        String cacheKey = "user:credit:profile:" + userId;
        // 这里需要通过反射或其他方式访问RedisTemplate，暂时跳过缓存清除
        
        log.info("更新用户 {} 的统计数据: 总上报={}, 通过={}", userId, totalReports, approvedReports);
    }
} 