package com.community.credit.service;

/**
 * DeepSeek AI服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface DeepSeekService {
    
    /**
     * 调用DeepSeek API进行智能问答
     * 
     * @param question 用户问题
     * @param context 上下文信息（公约内容）
     * @return AI回答
     */
    String askQuestion(String question, String context);
    
    /**
     * 检查DeepSeek服务是否可用
     * 
     * @return 是否可用
     */
    boolean isServiceAvailable();
} 