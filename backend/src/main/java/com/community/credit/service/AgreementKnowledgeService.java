package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.entity.AgreementKnowledge;
import com.community.credit.entity.CommunityAgreement;

import java.util.List;
import java.util.Map;

/**
 * 公约知识库服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface AgreementKnowledgeService extends IService<AgreementKnowledge> {

    /**
     * 根据公约内容创建或更新知识库
     * 
     * @param agreement 公约信息
     */
    void createOrUpdateKnowledge(CommunityAgreement agreement);

    /**
     * 根据公约ID删除知识库记录
     * 
     * @param agreementId 公约ID
     */
    void deleteByAgreementId(Integer agreementId);

    /**
     * 根据问题搜索相关知识库内容
     * 
     * @param question 用户问题
     * @return 相关知识库内容列表
     */
    List<AgreementKnowledge> searchRelevantKnowledge(String question);

    /**
     * 基于知识库回答用户问题
     * 
     * @param question 用户问题
     * @return 回答结果
     */
    Map<String, Object> answerQuestion(String question);

    /**
     * 提取文本关键词
     * 
     * @param content 文本内容
     * @return 关键词字符串
     */
    String extractKeywords(String content);

    /**
     * 生成内容摘要
     * 
     * @param content 原始内容
     * @return 摘要
     */
    String generateSummary(String content);

    /**
     * 初始化知识库（将现有公约导入知识库）
     */
    void initializeKnowledgeBase();
} 