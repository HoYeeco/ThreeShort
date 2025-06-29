package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.AgreementQuestionRequest;
import com.community.credit.dto.AgreementRequest;
import com.community.credit.entity.CommunityAgreement;

import java.util.List;
import java.util.Map;

/**
 * 社区公约服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface CommunityAgreementService extends IService<CommunityAgreement> {

    /**
     * 获取启用的公约列表
     * 
     * @return 公约列表
     */
    List<CommunityAgreement> getActiveAgreements();

    /**
     * 创建公约
     * 
     * @param request 公约信息
     * @return 公约ID
     */
    Integer createAgreement(AgreementRequest request);

    /**
     * 更新公约
     * 
     * @param id 公约ID
     * @param request 更新信息
     */
    void updateAgreement(Integer id, AgreementRequest request);

    /**
     * 删除公约
     * 
     * @param id 公约ID
     */
    void deleteAgreement(Integer id);

    /**
     * 启用/禁用公约
     * 
     * @param id 公约ID
     * @param isActive 是否启用
     */
    void toggleAgreementStatus(Integer id, Boolean isActive);

    /**
     * 调整公约排序
     * 
     * @param id 公约ID
     * @param newOrderNum 新的排序号
     */
    void updateAgreementOrder(Integer id, Integer newOrderNum);

    /**
     * AI智能问答
     * 
     * @param request 问答请求
     * @return 回答结果
     */
    Map<String, Object> askQuestion(AgreementQuestionRequest request);

    /**
     * 获取公约学习统计
     * 
     * @param userId 用户ID
     * @return 学习统计
     */
    Map<String, Object> getLearningStats(Integer userId);

    /**
     * 记录学习进度
     * 
     * @param userId 用户ID
     * @param agreementId 公约ID
     * @param action 学习行为（view/complete/question）
     */
    void recordLearningProgress(Integer userId, Integer agreementId, String action);
} 