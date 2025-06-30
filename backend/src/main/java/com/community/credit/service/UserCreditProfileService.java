package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.entity.UserCreditProfile;

import java.util.List;
import java.util.Map;

/**
 * 用户信用档案服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface UserCreditProfileService extends IService<UserCreditProfile> {

    /**
     * 根据用户ID获取信用档案
     */
    UserCreditProfile getByUserId(Integer userId);

    /**
     * 更新用户信用分数
     */
    void updateCreditScore(Integer userId, Integer scoreChange, String reason);

    /**
     * 获取用户信用等级
     */
    String getCreditLevel(Integer currentScore);

    /**
     * 计算信用等级
     */
    String calculateCreditLevel(Integer currentScore);

    /**
     * 更新用户奖励积分
     */
    void updateRewardPoints(Integer userId, Integer pointsChange, String reason);

    /**
     * 更新用户分数（内部方法）
     */
    void updateUserScore(Integer userId, Integer scoreChange);

    /**
     * 获取或创建用户信用档案
     */
    UserCreditProfile getOrCreateUserProfile(Integer userId);

    /**
     * 获取用户信用档案（不创建）
     */
    UserCreditProfile getUserProfile(Integer userId);

    /**
     * 获取用户信用统计数据
     */
    Map<String, Object> getUserCreditStats(Integer userId);

    /**
     * 批量获取用户信用档案
     */
    List<UserCreditProfile> getUserProfiles(List<Integer> userIds);

    /**
     * 获取信用等级分布统计
     */
    Map<String, Object> getCreditLevelDistribution();

    /**
     * 获取积分排行榜
     */
    List<Map<String, Object>> getScoreRankings(Integer limit);

    /**
     * 获取信用排行榜
     */
    List<UserCreditProfile> getCreditRanking(Integer limit);

    /**
     * 重新计算用户信用分数
     */
    void recalculateUserCredit(Integer userId);

    /**
     * 生成用户评分记录
     */
    void generateScoreRecord(Integer userId, String period);

    /**
     * 批量更新用户信用等级
     */
    void batchUpdateCreditLevels();

    /**
     * 批量生成周期评分记录
     */
    void batchGenerateScoreRecord(String period);

    /**
     * 获取用户积分历史
     */
    List<Map<String, Object>> getUserScoreHistory(Integer userId);

    /**
     * 分页查询用户信用档案列表
     */
    Map<String, Object> getUserCreditProfileList(Integer page, Integer size, String keyword, String creditLevel);

    /**
     * 检查用户是否可以兑换商品
     */
    boolean canExchangeProduct(Integer userId, Integer requiredPoints, List<String> eligibleLevels);

    /**
     * 获取高信用等级用户数量（AA和AAA等级）
     */
    long getHighCreditUserCount();

    /**
     * 清除用户信用档案缓存
     */
    void clearUserProfileCache(Integer userId);
} 