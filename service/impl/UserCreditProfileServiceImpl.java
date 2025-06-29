package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.entity.UserCreditProfile;
import com.community.credit.mapper.UserCreditProfileMapper;
import com.community.credit.mapper.CreditReportMapper;
import com.community.credit.service.CreditScoreRecordService;
import com.community.credit.service.UserCreditProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户信用档案服务实现
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserCreditProfileServiceImpl extends ServiceImpl<UserCreditProfileMapper, UserCreditProfile> 
        implements UserCreditProfileService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CreditReportMapper creditReportMapper;
    
    @Autowired
    private CreditScoreRecordService creditScoreRecordService;

    public UserCreditProfileServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserCreditProfile getByUserId(Integer userId) {
        String cacheKey = "user:credit:profile:" + userId;
        
        // 先从缓存获取
        UserCreditProfile cached = (UserCreditProfile) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        UserCreditProfile profile = this.getOne(new QueryWrapper<UserCreditProfile>().eq("user_id", userId));
        if (profile != null) {
            // 存入缓存，5分钟过期
            redisTemplate.opsForValue().set(cacheKey, profile, 5, TimeUnit.MINUTES);
        }

        return profile;
    }

    @Override
    @Transactional
    public void updateCreditScore(Integer userId, Integer scoreChange, String reason) {
        UserCreditProfile profile = getOrCreateUserProfile(userId);
        
        // 记录原始分数用于日志
        Integer originalScore = profile.getCurrentScore();
        
        // 计算新分数
        Integer newScore = originalScore + scoreChange;
        if (newScore < 0) {
            newScore = 0;
        }
        // 修改信用分上限为100分
        if (newScore > 100) {
            newScore = 100;
        }
        
        // 更新分数和等级
        profile.setCurrentScore(newScore);
        profile.setCreditLevel(calculateCreditLevel(newScore));
        profile.setLastScoreUpdate(LocalDateTime.now());
        
        updateById(profile);
        
        // 清除缓存，确保数据一致性
        String cacheKey = "user:credit:profile:" + userId;
        redisTemplate.delete(cacheKey);
        
        // 记录日志
        log.info("用户 {} 信用分数更新: {} -> {}, 原因: {}", 
            userId, originalScore, newScore, reason);
    }

    @Override
    public String getCreditLevel(Integer currentScore) {
        return calculateCreditLevel(currentScore);
    }

    @Override
    public String calculateCreditLevel(Integer currentScore) {
        if (currentScore >= 100) {
            return "AAA";
        } else if (currentScore >= 90) {
            return "AA";
        } else if (currentScore >= 80) {
            return "A";
        } else if (currentScore >= 60) {
            return "B";
        } else if (currentScore >= 40) {
            return "C";
        } else {
            return "D";
        }
    }

    @Override
    @Transactional
    public void updateRewardPoints(Integer userId, Integer pointsChange, String reason) {
        UserCreditProfile profile = getOrCreateUserProfile(userId);
        
        // 记录原始积分用于日志
        Integer originalPoints = profile.getRewardPoints();
        
        Integer newPoints = originalPoints + pointsChange;
        if (newPoints < 0) {
            newPoints = 0;
        }
        
        profile.setRewardPoints(newPoints);
        updateById(profile);
        
        // 清除缓存，确保数据一致性
        String cacheKey = "user:credit:profile:" + userId;
        redisTemplate.delete(cacheKey);
        
        log.info("用户 {} 奖励积分更新: {} -> {}, 原因: {}", 
            userId, originalPoints, newPoints, reason);
    }

    @Override
    public void updateUserScore(Integer userId, Integer scoreChange) {
        updateCreditScore(userId, scoreChange, "系统自动更新");
    }

    @Override
    public UserCreditProfile getOrCreateUserProfile(Integer userId) {
        UserCreditProfile profile = getByUserId(userId);
        if (profile == null) {
            profile = new UserCreditProfile();
            profile.setUserId(userId);
            profile.setCurrentScore(60); // 初始分数
            profile.setRewardPoints(0);
            profile.setCreditLevel(calculateCreditLevel(60));
            profile.setTotalReports(0);
            profile.setApprovedReports(0);
            profile.setLastScoreUpdate(LocalDateTime.now());
            
            this.save(profile);
            log.info("为用户 {} 创建新的信用档案", userId);
        }
        return profile;
    }
    
    /**
     * 仅获取用户信用档案，不自动创建
     */
    public UserCreditProfile getUserProfile(Integer userId) {
        return getByUserId(userId);
    }

    @Override
    public Map<String, Object> getUserCreditStats(Integer userId) {
        UserCreditProfile profile = getOrCreateUserProfile(userId);
        Map<String, Object> stats = new HashMap<>();
        
        // 基础信息
        stats.put("currentScore", profile.getCurrentScore());
        stats.put("creditLevel", profile.getCreditLevel());
        stats.put("rewardPoints", profile.getRewardPoints());
        stats.put("totalReports", profile.getTotalReports());
        stats.put("approvedReports", profile.getApprovedReports());
        
        // 计算通过率
        double approvalRate = profile.getTotalReports() > 0 ? 
            (double) profile.getApprovedReports() / profile.getTotalReports() * 100 : 0;
        stats.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
        
        // 距离下一等级
        String nextLevel = getNextCreditLevel(profile.getCurrentScore());
        Integer nextLevelScore = getNextLevelScore(profile.getCurrentScore());
        stats.put("nextLevel", nextLevel);
        stats.put("nextLevelScore", nextLevelScore);
        stats.put("scoreToNext", nextLevelScore - profile.getCurrentScore());
        
        return stats;
    }

    @Override
    public Map<String, Object> getCreditLevelDistribution() {
        List<UserCreditProfile> allProfiles = this.list();
        Map<String, Object> distribution = new HashMap<>();
        Map<String, Integer> levelCount = new HashMap<>();
        
        // 初始化计数
        String[] levels = {"AAA", "AA", "A", "B", "C", "D"};
        for (String level : levels) {
            levelCount.put(level, 0);
        }
        
        // 统计各等级数量
        for (UserCreditProfile profile : allProfiles) {
            String level = profile.getCreditLevel();
            levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
        }
        
        distribution.put("levelCount", levelCount);
        distribution.put("totalUsers", allProfiles.size());
        
        // 计算百分比
        Map<String, Double> levelPercentage = new HashMap<>();
        for (String level : levels) {
            double percentage = allProfiles.size() > 0 ? 
                (double) levelCount.get(level) / allProfiles.size() * 100 : 0;
            levelPercentage.put(level, Math.round(percentage * 100.0) / 100.0);
        }
        distribution.put("levelPercentage", levelPercentage);
        
        return distribution;
    }

    @Override
    public List<UserCreditProfile> getCreditRanking(Integer limit) {
        return this.baseMapper.getCreditRankingWithUserName(limit);
    }

    @Override
    @Transactional
    public void recalculateUserCredit(Integer userId) {
        UserCreditProfile profile = getOrCreateUserProfile(userId);
        
        // 重新计算信用分数的逻辑
        // 这里可以根据用户的历史行为重新计算分数
        
        // 更新信用等级
        profile.setCreditLevel(calculateCreditLevel(profile.getCurrentScore()));
        profile.setLastScoreUpdate(LocalDateTime.now());
        
        updateById(profile);
        
        // 清除缓存，确保数据一致性
        String cacheKey = "user:credit:profile:" + userId;
        redisTemplate.delete(cacheKey);
        
        log.info("用户 {} 信用分数重新计算完成", userId);
    }

    @Override
    public void generateScoreRecord(Integer userId, String period) {
        UserCreditProfile profile = getByUserId(userId);
        if (profile != null) {
            creditScoreRecordService.generateUserScoreRecord(userId, period);
        }
    }

    @Override
    public void batchGenerateScoreRecord(String period) {
        List<UserCreditProfile> allProfiles = this.list();
        for (UserCreditProfile profile : allProfiles) {
            try {
                creditScoreRecordService.generateUserScoreRecord(profile.getUserId(), period);
            } catch (Exception e) {
                log.error("为用户 {} 生成周期 {} 评分记录失败", profile.getUserId(), period, e);
            }
        }
        log.info("批量生成周期 {} 评分记录完成，处理用户数: {}", period, allProfiles.size());
    }

    @Override
    public List<Map<String, Object>> getUserScoreHistory(Integer userId) {
        // 首先生成或更新当前的实时评分记录，确保历史记录包含最新数据
        try {
            creditScoreRecordService.generateCurrentScoreRecord(userId);
        } catch (Exception e) {
            log.warn("生成用户 {} 的实时评分记录失败: {}", userId, e.getMessage());
        }
        
        return creditScoreRecordService.getUserScoreTrend(userId);
    }
    
    /**
     * 获取下一个信用等级
     */
    private String getNextCreditLevel(Integer currentScore) {
        if (currentScore < 40) return "C";
        if (currentScore < 60) return "B";
        if (currentScore < 80) return "A";
        if (currentScore < 100) return "AA";
        if (currentScore < 120) return "AAA";
        return "AAA";
    }
    
    /**
     * 获取下一等级所需分数
     */
    private Integer getNextLevelScore(Integer currentScore) {
        if (currentScore < 40) return 40;
        if (currentScore < 60) return 60;
        if (currentScore < 80) return 80;
        if (currentScore < 100) return 100;
        if (currentScore < 120) return 120;
        return 120;
    }

    @Override
    public List<UserCreditProfile> getUserProfiles(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return this.listByIds(userIds);
    }

    @Override
    public Map<String, Object> getUserCreditProfileList(Integer page, Integer size, String keyword, String creditLevel) {
        // 使用Mapper直接查询带用户信息的档案列表
        List<UserCreditProfile> records = this.baseMapper.getUserCreditProfileListWithUserInfo(
            (page - 1) * size, size, keyword, creditLevel);
        
        // 获取总数
        Long total = this.baseMapper.getUserCreditProfileCount(keyword, creditLevel);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("current", page);
        result.put("size", size);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getScoreRankings(Integer limit) {
        // 获取排行榜数据
        List<UserCreditProfile> rankings = getCreditRanking(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (int i = 0; i < rankings.size(); i++) {
            UserCreditProfile profile = rankings.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("userId", profile.getUserId());
            item.put("currentScore", profile.getCurrentScore());
            item.put("creditLevel", profile.getCreditLevel());
            item.put("rewardPoints", profile.getRewardPoints());
            result.add(item);
        }
        
        return result;
    }

    @Override
    public void batchUpdateCreditLevels() {
        List<UserCreditProfile> allProfiles = this.list();
        for (UserCreditProfile profile : allProfiles) {
            String newLevel = calculateCreditLevel(profile.getCurrentScore());
            if (!newLevel.equals(profile.getCreditLevel())) {
                profile.setCreditLevel(newLevel);
                updateById(profile);
            }
        }
        log.info("批量更新信用等级完成，处理用户数: {}", allProfiles.size());
    }

    @Override
    public boolean canExchangeProduct(Integer userId, Integer requiredPoints, List<String> eligibleLevels) {
        UserCreditProfile profile = getByUserId(userId);
        if (profile == null) {
            return false;
        }
        
        // 检查积分是否足够
        if (profile.getRewardPoints() < requiredPoints) {
            return false;
        }
        
        // 检查信用等级是否符合要求
        if (eligibleLevels != null && !eligibleLevels.isEmpty()) {
            return eligibleLevels.contains(profile.getCreditLevel());
        }
        
        return true;
    }

    @Override
    public long getHighCreditUserCount() {
        QueryWrapper<UserCreditProfile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("credit_level", "AA", "AAA");
        return this.count(queryWrapper);
    }

    @Override
    public void clearUserProfileCache(Integer userId) {
        String cacheKey = "user:credit:profile:" + userId;
        redisTemplate.delete(cacheKey);
        log.debug("清除用户 {} 的信用档案缓存", userId);
    }
} 