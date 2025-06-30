package com.community.credit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.CreditReportQueryRequest;
import com.community.credit.dto.CreditReportRequest;
import com.community.credit.dto.CreditReportReviewRequest;
import com.community.credit.entity.CreditReport;

import java.util.Map;

/**
 * 信用行为上报服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface CreditReportService extends IService<CreditReport> {

    /**
     * 提交信用行为上报
     * 
     * @param request 上报请求
     */
    void submitReport(CreditReportRequest request);

    /**
     * 分页查询上报列表
     * 
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<CreditReport> queryReports(CreditReportQueryRequest request);

    /**
     * 获取上报详情
     * 
     * @param reportId 上报ID
     * @return 上报详情
     */
    CreditReport getReportDetail(Integer reportId);

    /**
     * 审核上报
     * 
     * @param reportId 上报ID
     * @param request 审核请求
     */
    void reviewReport(Integer reportId, CreditReportReviewRequest request);

    /**
     * 撤回上报
     * 
     * @param reportId 上报ID
     * @param userId 用户ID
     */
    void withdrawReport(Integer reportId, Integer userId);

    /**
     * 获取待审核数量
     * 
     * @return 待审核数量
     */
    Long getPendingCount();

    /**
     * 获取信用报告统计数据
     * 
     * @return 统计数据
     */
    Map<String, Object> getReportStatistics();
} 