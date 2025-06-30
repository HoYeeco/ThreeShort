package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.entity.CreditScoreRecord;
import com.community.credit.mapper.CreditReportMapper;
import com.community.credit.mapper.CreditScoreRecordMapper;
import com.community.credit.service.CreditScoreRecordService;
import com.community.credit.service.UserCreditProfileService;
import com.community.credit.entity.UserCreditProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信用评分记录服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CreditScoreRecordServiceImpl extends ServiceImpl<CreditScoreRecordMapper, CreditScoreRecord> implements CreditScoreRecordService {

    @Autowired
    private CreditReportMapper creditReportMapper;
    
    @Autowired
    @Lazy
    private UserCreditProfileService userCreditProfileService;

    @Override
    @Transactional
    public CreditScoreRecord generateUserScoreRecord(Integer userId, String period) {
        // 检查是否已存在该周期的记录
        if (isPeriodScoreExists(userId, period)) {
            log.warn("用户 {} 在周期 {} 的评分记录已存在", userId, period);
            return getOne(new QueryWrapper<CreditScoreRecord>()
                    .eq("user_id", userId)
                    .eq("score_period", period));
        }

        // 获取用户当前信用档案
        UserCreditProfile profile = userCreditProfileService.getOrCreateUserProfile(userId);
        Integer currentScore = profile.getCurrentScore();
        
        // 计算奖励积分转化（使用新规则）
        Integer rewardPoints = 0;
        Integer finalScore = currentScore;
        
        if (currentScore > 70) {
            if (currentScore > 100) {
                // 超过100分的部分：100%转化
                int over100Points = currentScore - 100;
                // 70-100分的部分：50%转化
                int between70And100Points = (int) Math.floor((100 - 70) * 0.5);
                rewardPoints = over100Points + between70And100Points;
            } else {
                // 70-100分的部分：50%转化
                rewardPoints = (int) Math.floor((currentScore - 70) * 0.5);
            }
            // 超过70分的信用分重置为70分
            finalScore = 70;
        }
        
        // 计算周期的开始和结束时间
        LocalDateTime periodStart = LocalDateTime.parse(period + " 00:00:00", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime periodEnd = periodStart.plusWeeks(1).minusSeconds(1); // 周期结束时间为下周一前一秒
        
        // 如果周期结束时间是未来时间，则使用当前时间
        LocalDateTime actualPeriodEnd = periodEnd.isAfter(LocalDateTime.now()) ? 
                LocalDateTime.now() : periodEnd;
        
        // 获取用户在该周期的上报统计（用于记录详情）
        Map<String, Object> reportStats = creditReportMapper.getUserReportStatsByPeriod(
                userId, 
                periodStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                actualPeriodEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        Integer approvedReports = reportStats.get("approved_count") != null ? 
                ((Number) reportStats.get("approved_count")).intValue() : 0;

        // 创建评分记录
        CreditScoreRecord record = new CreditScoreRecord();
        record.setUserId(userId);
        record.setScorePeriod(period);
        record.setPeriodScore(currentScore); // 记录转化前的分数
        record.setRewardPointsGained(rewardPoints);
        record.setCalculationTime(LocalDateTime.now());
        
        // 将详细信息转换为JSON字符串
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("originalScore", currentScore);
        detailsMap.put("rewardPointsGained", rewardPoints);
        detailsMap.put("conversionRule", "70-100分按50%转化，100分以上按100%转化");
        detailsMap.put("calculationTime", LocalDateTime.now().toString());
        
        // 简单的JSON序列化（生产环境建议使用Jackson或Gson）
        String detailsJson = String.format(
            "{\"originalScore\":%d,\"rewardPointsGained\":%d,\"conversionRule\":\"%s\",\"calculationTime\":\"%s\"}",
            currentScore, rewardPoints, "70-100分按50%转化，100分以上按100%转化", LocalDateTime.now()
        );
        record.setDetails(detailsJson);

        this.save(record);
        
        // 如果有奖励积分，增加到用户档案
        if (rewardPoints > 0) {
            userCreditProfileService.updateRewardPoints(userId, rewardPoints, 
                String.format("周期%s信用分转化获得奖励积分", period));
        }
        
        // 根据新规则重置信用分
        if (currentScore > 70) {
            // 直接设置为70分，不使用updateCreditScore避免触发其他逻辑
            profile.setCurrentScore(finalScore);
            profile.setCreditLevel(userCreditProfileService.calculateCreditLevel(finalScore));
            profile.setLastScoreUpdate(LocalDateTime.now());
            userCreditProfileService.updateById(profile);
            
            log.info("用户 {} 信用分从 {} 重置为 {} 分", userId, currentScore, finalScore);
        }
        
        log.info("为用户 {} 生成周期 {} 评分记录: 原始分数={}, 奖励积分={}, 重置后分数={}", 
                userId, period, currentScore, rewardPoints, finalScore);

        return record;
    }

    /**
     * 为用户生成当前实时评分记录（用于展示最新的分数变化）
     * 
     * @param userId 用户ID
     * @return 评分记录
     */
    @Transactional
    public CreditScoreRecord generateCurrentScoreRecord(Integer userId) {
        // 获取用户当前信用档案
        UserCreditProfile profile = userCreditProfileService.getOrCreateUserProfile(userId);
        Integer currentScore = profile.getCurrentScore();
        
        // 生成当前日期作为周期
        String currentPeriod = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 检查今天是否已有记录，如果有则更新，没有则创建
        QueryWrapper<CreditScoreRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("score_period", currentPeriod);
        CreditScoreRecord existingRecord = getOne(queryWrapper);
        
        if (existingRecord != null) {
            // 更新现有记录
            existingRecord.setPeriodScore(currentScore);
            existingRecord.setCalculationTime(LocalDateTime.now());
            
            String detailsJson = String.format(
                "{\"currentScore\":%d,\"updateTime\":\"%s\",\"note\":\"实时更新记录\"}",
                currentScore, LocalDateTime.now()
            );
            existingRecord.setDetails(detailsJson);
            
            updateById(existingRecord);
            log.info("更新用户 {} 的实时评分记录: 分数={}", userId, currentScore);
            return existingRecord;
        } else {
            // 创建新记录
            CreditScoreRecord record = new CreditScoreRecord();
            record.setUserId(userId);
            record.setScorePeriod(currentPeriod);
            record.setPeriodScore(currentScore);
            record.setRewardPointsGained(0); // 实时记录不涉及积分转化
            record.setCalculationTime(LocalDateTime.now());
            
            String detailsJson = String.format(
                "{\"currentScore\":%d,\"createTime\":\"%s\",\"note\":\"实时创建记录\"}",
                currentScore, LocalDateTime.now()
            );
            record.setDetails(detailsJson);
            
            save(record);
            log.info("为用户 {} 创建实时评分记录: 分数={}", userId, currentScore);
            return record;
        }
    }

    @Override
    public void batchGenerateScoreRecord(String period) {
        log.info("开始批量生成周期 {} 的评分记录", period);
        
        // 获取所有用户ID（通过查询用户信用档案表）
        List<Integer> userIds = baseMapper.getAllActiveUserIds();
        
        int successCount = 0;
        int failCount = 0;
        
        for (Integer userId : userIds) {
            try {
                generateUserScoreRecord(userId, period);
                successCount++;
            } catch (Exception e) {
                log.error("为用户 {} 生成周期 {} 评分记录失败", userId, period, e);
                failCount++;
            }
        }
        
        log.info("批量生成周期 {} 评分记录完成，成功: {}, 失败: {}, 总计: {}", 
                period, successCount, failCount, userIds.size());
    }

    @Override
    public List<CreditScoreRecord> getUserScoreHistory(Integer userId) {
        return baseMapper.getUserScoreHistory(userId);
    }

    @Override
    public List<Map<String, Object>> getUserScoreTrend(Integer userId) {
        return baseMapper.getUserScoreTrend(userId);
    }

    @Override
    public boolean isPeriodScoreExists(Integer userId, String period) {
        QueryWrapper<CreditScoreRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("score_period", period);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public String getLatestScorePeriod() {
        QueryWrapper<CreditScoreRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("score_period")
                   .orderByDesc("calculation_time")
                   .last("LIMIT 1");
        
        CreditScoreRecord latestRecord = getOne(queryWrapper);
        return latestRecord != null ? latestRecord.getScorePeriod() : null;
    }

    @Override
    public void executePeriodicScoreConversion() {
        executePeriodicScoreConversion(false);
    }
    
    /**
     * 执行周期性信用分转化
     * @param forceRerun 是否强制重新执行（忽略已存在的记录）
     */
    public void executePeriodicScoreConversion(boolean forceRerun) {
        log.info("开始执行周期性信用分转化...（强制重新执行：{}）", forceRerun);
        
        try {
            String currentPeriod = getCurrentPeriod();
            
            // 获取所有用户的信用档案
            List<UserCreditProfile> allProfiles = userCreditProfileService.list();
            
            int totalUsers = allProfiles.size();
            int processedUsers = 0;
            int totalRewardPoints = 0;
            
            for (UserCreditProfile profile : allProfiles) {
                try {
                    // 检查是否已经处理过这个周期（除非强制重新执行）
                    if (!forceRerun && isPeriodScoreExists(profile.getUserId(), currentPeriod)) {
                        log.debug("用户 {} 的周期 {} 评分记录已存在，跳过", profile.getUserId(), currentPeriod);
                        continue;
                    }
                    
                    Integer currentScore = profile.getCurrentScore();
                    Integer rewardPointsGained = 0;
                    Integer newScore = currentScore;
                    
                    // 新的转化规则：
                    // 1. 超过100分的部分100%转化为奖励分
                    // 2. 70-100分的部分50%转化为奖励分  
                    // 3. 超过70分的信用分重置为70分
                    // 4. 小于70分的信用分保持不变
                    
                    if (currentScore > 70) {
                        // 计算奖励积分转化
                        if (currentScore > 100) {
                            // 超过100分的部分：100%转化
                            int over100Points = currentScore - 100;
                            // 70-100分的部分：50%转化
                            int between70And100Points = (int) Math.floor((100 - 70) * 0.5);
                            rewardPointsGained = over100Points + between70And100Points;
                        } else {
                            // 70-100分的部分：50%转化
                            rewardPointsGained = (int) Math.floor((currentScore - 70) * 0.5);
                        }
                        
                        // 超过70分的信用分重置为70分
                        newScore = 70;
                        profile.setCurrentScore(newScore);
                    }
                    // 小于70分的信用分保持不变（不需要处理）
                    
                    // 更新奖励积分
                    if (rewardPointsGained > 0) {
                        Integer currentRewardPoints = profile.getRewardPoints();
                        if (currentRewardPoints == null) {
                            currentRewardPoints = 0;
                        }
                        profile.setRewardPoints(currentRewardPoints + rewardPointsGained);
                        totalRewardPoints += rewardPointsGained;
                    }
                    
                    // 更新信用等级
                    profile.setCreditLevel(userCreditProfileService.calculateCreditLevel(profile.getCurrentScore()));
                    profile.setLastScoreUpdate(LocalDateTime.now());
                    
                    // 保存更新的档案
                    userCreditProfileService.updateById(profile);
                    
                    // 如果强制重新执行，先删除已存在的记录
                    if (forceRerun && isPeriodScoreExists(profile.getUserId(), currentPeriod)) {
                        QueryWrapper<CreditScoreRecord> deleteWrapper = new QueryWrapper<>();
                        deleteWrapper.eq("user_id", profile.getUserId()).eq("score_period", currentPeriod);
                        remove(deleteWrapper);
                        log.info("删除用户 {} 周期 {} 的旧评分记录", profile.getUserId(), currentPeriod);
                    }
                    
                    // 创建评分记录
                    CreditScoreRecord record = new CreditScoreRecord();
                    record.setUserId(profile.getUserId());
                    record.setScorePeriod(currentPeriod);
                    record.setPeriodScore(currentScore); // 记录转化前的分数
                    record.setRewardPointsGained(rewardPointsGained);
                    record.setCalculationTime(LocalDateTime.now());
                    
                    // 将详细信息转换为JSON字符串
                    Map<String, Object> detailsMap = new HashMap<>();
                    detailsMap.put("originalScore", currentScore);
                    detailsMap.put("finalScore", newScore);
                    detailsMap.put("rewardPointsGained", rewardPointsGained);
                    detailsMap.put("conversionRule", "超过100分部分100%转化，70-100分部分50%转化，超过70分重置为70分");
                    detailsMap.put("calculationTime", LocalDateTime.now().toString());
                    detailsMap.put("forceRerun", forceRerun);
                    
                    // 简单的JSON序列化（生产环境建议使用Jackson或Gson）
                    String detailsJson = String.format(
                        "{\"originalScore\":%d,\"finalScore\":%d,\"rewardPointsGained\":%d,\"conversionRule\":\"%s\",\"calculationTime\":\"%s\",\"forceRerun\":%b}",
                        currentScore, newScore, rewardPointsGained, "超过100分部分100%转化，70-100分部分50%转化，超过70分重置为70分", LocalDateTime.now(), forceRerun
                    );
                    record.setDetails(detailsJson);
                    
                    // 保存评分记录
                    save(record);
                    
                    processedUsers++;
                    
                    log.info("用户 {} 周期转化完成：原分数 {}，重置后分数 {}，获得奖励积分 {}", 
                        profile.getUserId(), currentScore, newScore, rewardPointsGained);
                        
                } catch (Exception e) {
                    log.error("处理用户 {} 的周期转化时发生错误", profile.getUserId(), e);
                }
            }
            
            log.info("周期性信用分转化完成！处理用户数：{}/{}，总奖励积分：{}", 
                processedUsers, totalUsers, totalRewardPoints);
                
        } catch (Exception e) {
            log.error("执行周期性信用分转化失败", e);
            throw new RuntimeException("周期性信用分转化失败", e);
        }
    }

    /**
     * 生成当前周期标识
     * 
     * @return 周期标识（格式：yyyy-MM-dd）
     */
    public static String getCurrentPeriod() {
        LocalDateTime now = LocalDateTime.now();
        // 每周一开始新的评分周期，获取本周一的日期
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        LocalDateTime weekStart = now.minusDays(dayOfWeek - 1);
        return weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * 获取上一个评分周期标识
     * 
     * @return 上一周期标识（格式：yyyy-MM-dd）
     */
    public static String getLastPeriod() {
        LocalDateTime now = LocalDateTime.now();
        // 获取上周一的日期
        int dayOfWeek = now.getDayOfWeek().getValue();
        LocalDateTime lastWeekStart = now.minusDays(dayOfWeek - 1).minusWeeks(1);
        return lastWeekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
} 