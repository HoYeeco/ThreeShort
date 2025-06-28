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
import com.community.credit.mapper.CreditReportMapper;
import com.community.credit.service.CreditBehaviorTypeService;
import com.community.credit.service.CreditReportService;
import com.community.credit.service.UserCreditProfileService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 信用行为上报服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Service
public class CreditReportServiceImpl extends ServiceImpl<CreditReportMapper, CreditReport> implements CreditReportService {

    @Autowired
    private CreditBehaviorTypeService creditBehaviorTypeService;

    @Autowired
    private UserCreditProfileService userCreditProfileService;

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
    }

    @Override
    public IPage<CreditReport> queryReports(CreditReportQueryRequest request) {
        Page<CreditReport> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<CreditReport> queryWrapper = new QueryWrapper<>();

        // 构建查询条件
        if (request.getUserId() != null) {
            queryWrapper.eq("user_id", request.getUserId());
        }
        if (request.getBehaviorTypeId() != null) {
            queryWrapper.eq("behavior_type_id", request.getBehaviorTypeId());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq("status", request.getStatus());
        }
        if (request.getReviewerId() != null) {
            queryWrapper.eq("reviewer_id", request.getReviewerId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper.like("title", request.getKeyword())
                    .or().like("description", request.getKeyword()));
        }
        
        // 时间范围查询
        if (StringUtils.hasText(request.getStartTime())) {
            LocalDateTime startTime = LocalDateTime.parse(request.getStartTime() + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.ge("report_time", startTime);
        }
        if (StringUtils.hasText(request.getEndTime())) {
            LocalDateTime endTime = LocalDateTime.parse(request.getEndTime() + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            queryWrapper.le("report_time", endTime);
        }

        queryWrapper.orderByDesc("created_time");
        return this.page(page, queryWrapper);
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
            // 获取行为类型的分数规则
            CreditBehaviorType behaviorType = creditBehaviorTypeService.getById(report.getBehaviorTypeId());
            if (behaviorType != null) {
                report.setScoreAwarded(behaviorType.getScoreRule());
                
                // 更新用户信用分数
                userCreditProfileService.updateCreditScore(report.getUserId(), 
                    behaviorType.getScoreRule(), "信用行为上报审核通过");
            }
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
} 