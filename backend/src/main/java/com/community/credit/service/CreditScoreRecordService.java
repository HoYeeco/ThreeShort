package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.entity.CreditScoreRecord;

import java.util.List;
import java.util.Map;

/**
 * 信用评分记录服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface CreditScoreRecordService extends IService<CreditScoreRecord> {

    /**
     * 生成用户周期评分记录
     * 
     * @param userId 用户ID
     * @param period 评分周期
     * @return 评分记录
     */
    CreditScoreRecord generateUserScoreRecord(Integer userId, String period);

    /**
     * 批量生成周期评分记录
     */
    void batchGenerateScoreRecord(String period);

    /**
     * 获取用户评分历史
     * 
     * @param userId 用户ID
     * @return 评分历史
     */
    List<CreditScoreRecord> getUserScoreHistory(Integer userId);

    /**
     * 获取用户评分趋势
     * 
     * @param userId 用户ID
     * @return 趋势数据
     */
    List<Map<String, Object>> getUserScoreTrend(Integer userId);

    /**
     * 检查周期评分记录是否存在
     * 
     * @param userId 用户ID
     * @param period 评分周期
     * @return 是否存在
     */
    boolean isPeriodScoreExists(Integer userId, String period);

    /**
     * 获取最新评分周期
     * 
     * @return 最新周期
     */
    String getLatestScorePeriod();

    /**
     * 为用户生成当前实时评分记录（用于展示最新的分数变化）
     * 
     * @param userId 用户ID
     * @return 评分记录
     */
    CreditScoreRecord generateCurrentScoreRecord(Integer userId);

    /**
     * 执行周期性信用分转化
     */
    void executePeriodicScoreConversion();
} 