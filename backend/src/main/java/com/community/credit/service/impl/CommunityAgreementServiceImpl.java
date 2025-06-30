package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.AgreementQuestionRequest;
import com.community.credit.dto.AgreementRequest;
import com.community.credit.entity.CommunityAgreement;
import com.community.credit.mapper.CommunityAgreementMapper;
import com.community.credit.service.AgreementKnowledgeService;
import com.community.credit.service.CommunityAgreementService;
import com.community.credit.service.DeepSeekService;
import com.community.credit.service.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 社区公约服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class CommunityAgreementServiceImpl extends ServiceImpl<CommunityAgreementMapper, CommunityAgreement> implements CommunityAgreementService {

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private AgreementKnowledgeService agreementKnowledgeService;

    @Override
    public List<CommunityAgreement> getActiveAgreements() {
        String cacheKey = "agreements:active";
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<CommunityAgreement> cached = (List<CommunityAgreement>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        List<CommunityAgreement> agreements = baseMapper.getActiveAgreements();
        
        // 存入缓存，30分钟过期
        redisTemplate.opsForValue().set(cacheKey, agreements, 30, TimeUnit.MINUTES);
        
        return agreements;
    }

    @Override
    @Transactional
    public Integer createAgreement(AgreementRequest request) {
        CommunityAgreement agreement = new CommunityAgreement();
        BeanUtils.copyProperties(request, agreement);
        
        // 如果没有指定排序号，设为最大值+1
        if (agreement.getOrderNum() == null || agreement.getOrderNum() == 0) {
            LambdaQueryWrapper<CommunityAgreement> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(CommunityAgreement::getOrderNum);
            wrapper.orderByDesc(CommunityAgreement::getOrderNum);
            wrapper.last("LIMIT 1");
            
            CommunityAgreement lastAgreement = this.getOne(wrapper);
            int maxOrder = lastAgreement != null && lastAgreement.getOrderNum() != null ? 
                lastAgreement.getOrderNum() : 0;
            agreement.setOrderNum(maxOrder + 1);
        }

        this.save(agreement);
        
        // 同步更新知识库
        agreementKnowledgeService.createOrUpdateKnowledge(agreement);
        
        // 清除缓存和学习统计缓存
        clearAgreementCache();
        clearAllLearningStatsCache();
        
        log.info("创建公约成功，ID: {}, 标题: {}", agreement.getId(), agreement.getTitle());
        return agreement.getId();
    }

    @Override
    @Transactional
    public void updateAgreement(Integer id, AgreementRequest request) {
        CommunityAgreement agreement = this.getById(id);
        if (agreement == null) {
            throw new RuntimeException("公约不存在");
        }

        String originalTitle = agreement.getTitle();
        BeanUtils.copyProperties(request, agreement);
        agreement.setId(id); // 确保ID不被覆盖

        this.updateById(agreement);
        
        // 同步更新知识库
        agreementKnowledgeService.createOrUpdateKnowledge(agreement);
        
        // 清除缓存和学习统计缓存
        clearAgreementCache();
        clearAllLearningStatsCache();
        
        log.info("更新公约成功，ID: {}, 原标题: {}, 新标题: {}", id, originalTitle, agreement.getTitle());
    }

    @Override
    @Transactional
    public void deleteAgreement(Integer id) {
        CommunityAgreement agreement = this.getById(id);
        if (agreement == null) {
            throw new RuntimeException("公约不存在");
        }

        this.removeById(id);
        
        // 从知识库中删除
        agreementKnowledgeService.deleteByAgreementId(id);
        
        // 清除缓存
        clearAgreementCache();
        
        // 清理所有用户的学习进度数据中的这个公约ID
        clearAgreementFromLearningProgress(id);
        
        log.info("删除公约成功，ID: {}, 标题: {}", id, agreement.getTitle());
    }

    @Override
    @Transactional
    public void toggleAgreementStatus(Integer id, Boolean isActive) {
        CommunityAgreement agreement = this.getById(id);
        if (agreement == null) {
            throw new RuntimeException("公约不存在");
        }

        agreement.setIsActive(isActive);
        this.updateById(agreement);
        
        // 同步更新知识库状态
        agreementKnowledgeService.createOrUpdateKnowledge(agreement);
        
        // 清除缓存和学习统计缓存
        clearAgreementCache();
        clearAllLearningStatsCache();
        
        log.info("{}公约成功，ID: {}, 标题: {}", isActive ? "启用" : "禁用", id, agreement.getTitle());
    }

    @Override
    @Transactional
    public void updateAgreementOrder(Integer id, Integer newOrderNum) {
        CommunityAgreement agreement = this.getById(id);
        if (agreement == null) {
            throw new RuntimeException("公约不存在");
        }

        Integer oldOrderNum = agreement.getOrderNum();
        agreement.setOrderNum(newOrderNum);
        this.updateById(agreement);
        
        // 清除缓存
        clearAgreementCache();
        
        log.info("调整公约排序成功，ID: {}, 排序: {} -> {}", id, oldOrderNum, newOrderNum);
    }

    @Override
    public Map<String, Object> askQuestion(AgreementQuestionRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取公约内容
            List<CommunityAgreement> agreements;
            if (request.getAgreementId() != null) {
                // 查询特定公约
                CommunityAgreement agreement = this.getById(request.getAgreementId());
                if (agreement != null && agreement.getIsActive()) {
                    agreements = Collections.singletonList(agreement);
                } else {
                    agreements = Collections.emptyList();
                }
            } else {
                // 查询所有启用的公约
                agreements = getActiveAgreements();
            }

            // 使用DeepSeek AI问答或回退到简化版逻辑
            String answer = generateAIAnswer(request.getQuestion(), agreements);
            
            result.put("question", request.getQuestion());
            result.put("answer", answer);
            result.put("timestamp", LocalDateTime.now());
            result.put("sessionId", request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString());
            
            // 可以在这里记录问答历史到数据库
            log.info("AI问答完成，问题: {}, 会话ID: {}", request.getQuestion(), result.get("sessionId"));
            
        } catch (Exception e) {
            log.error("AI问答失败", e);
            result.put("question", request.getQuestion());
            result.put("answer", "抱歉，暂时无法回答您的问题，请稍后重试。");
            result.put("error", true);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getLearningStats(Integer userId) {
        String cacheKey = "learning:stats:" + userId;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        Map<String, Object> cached = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Map<String, Object> stats = new HashMap<>();
        
        // 获取当前有效的公约列表
        List<CommunityAgreement> activeAgreements = this.list(new LambdaQueryWrapper<CommunityAgreement>()
            .eq(CommunityAgreement::getIsActive, true));
        long totalAgreements = activeAgreements.size();
        
        // 获取有效公约的ID集合
        Set<Integer> activeAgreementIds = activeAgreements.stream()
            .map(CommunityAgreement::getId)
            .collect(Collectors.toSet());
        
        // 从Redis获取学习进度，并过滤掉已删除的公约
        String progressKey = "learning:progress:" + userId;
        Set<Object> viewedAgreements = redisTemplate.opsForSet().members(progressKey);
        int viewedCount = 0;
        if (viewedAgreements != null) {
            viewedCount = (int) viewedAgreements.stream()
                .filter(id -> {
                    try {
                        Integer agreementId = Integer.valueOf(id.toString());
                        return activeAgreementIds.contains(agreementId);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .count();
        }
        
        // 获取完成学习的公约数量（只计算当前生效的公约）
        String completeKey = "learning:complete:" + userId;
        Set<Object> completedAgreements = redisTemplate.opsForSet().members(completeKey);
        int completedCount = 0;
        if (completedAgreements != null) {
            completedCount = (int) completedAgreements.stream()
                .filter(id -> {
                    try {
                        Integer agreementId = Integer.valueOf(id.toString());
                        return activeAgreementIds.contains(agreementId);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .count();
        }
        
        // 计算完成率（基于已查看的公约数量）
        double completionRate = totalAgreements > 0 ? (double) viewedCount / totalAgreements * 100 : 0;
        // 计算学习完成率（基于标记为完成的公约数量）
        double studyCompletionRate = totalAgreements > 0 ? (double) completedCount / totalAgreements * 100 : 0;
        
        stats.put("totalAgreements", totalAgreements);
        stats.put("viewedAgreements", viewedCount);
        stats.put("completedAgreements", completedCount);
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        stats.put("studyCompletionRate", Math.round(studyCompletionRate * 100.0) / 100.0);
        stats.put("lastStudyTime", redisTemplate.opsForValue().get("learning:lasttime:" + userId));
        
        // 获取问答次数
        String questionCountKey = "learning:questions:" + userId;
        Integer questionCount = (Integer) redisTemplate.opsForValue().get(questionCountKey);
        stats.put("questionCount", questionCount != null ? questionCount : 0);
        
        // 存入缓存，5分钟过期
        redisTemplate.opsForValue().set(cacheKey, stats, 5, TimeUnit.MINUTES);
        
        return stats;
    }

    @Override
    public void recordLearningProgress(Integer userId, Integer agreementId, String action) {
        String timestamp = LocalDateTime.now().toString();
        
        switch (action) {
            case "view":
                // 记录查看的公约
                String progressKey = "learning:progress:" + userId;
                redisTemplate.opsForSet().add(progressKey, agreementId);
                redisTemplate.expire(progressKey, 30, TimeUnit.DAYS);
                
                // 记录最后学习时间
                redisTemplate.opsForValue().set("learning:lasttime:" + userId, timestamp, 30, TimeUnit.DAYS);
                break;
                
            case "question":
                // 记录问答次数
                String questionCountKey = "learning:questions:" + userId;
                redisTemplate.opsForValue().increment(questionCountKey);
                redisTemplate.expire(questionCountKey, 30, TimeUnit.DAYS);
                break;
                
            case "complete":
                // 记录完成学习
                String completeKey = "learning:complete:" + userId;
                redisTemplate.opsForSet().add(completeKey, agreementId);
                redisTemplate.expire(completeKey, 30, TimeUnit.DAYS);
                break;
        }
        
        // 清除学习统计缓存
        redisTemplate.delete("learning:stats:" + userId);
        
        log.debug("记录学习进度，用户: {}, 公约: {}, 行为: {}", userId, agreementId, action);
    }

    /**
     * 生成AI回答（集成DeepSeek或使用简化版实现）
     */
    private String generateAIAnswer(String question, List<CommunityAgreement> agreements) {
        // 构建上下文信息
        StringBuilder context = new StringBuilder();
        for (CommunityAgreement agreement : agreements) {
            context.append("《").append(agreement.getTitle()).append("》\n");
            context.append(agreement.getContent()).append("\n\n");
        }
        
        // 如果DeepSeek服务可用，优先使用
        if (deepSeekService.isServiceAvailable()) {
            try {
                String aiAnswer = deepSeekService.askQuestion(question, context.toString());
                if (aiAnswer != null && !aiAnswer.trim().isEmpty()) {
                    log.info("使用DeepSeek AI回答问题: {}", question);
                    return aiAnswer;
                }
            } catch (Exception e) {
                log.warn("DeepSeek AI调用失败，回退到简化版回答", e);
            }
        }
        
        // 回退到简化版实现
        log.info("使用简化版AI回答问题: {}", question);
        return generateAnswer(question, agreements);
    }

    /**
     * 生成AI回答（简化版实现）
     */
    private String generateAnswer(String question, List<CommunityAgreement> agreements) {
        String lowerQuestion = question.toLowerCase();
        
        // 关键词匹配
        Map<String, String> keywordResponses = new HashMap<>();
        keywordResponses.put("垃圾", "根据社区环境维护公约，居民应当不在公共区域乱丢垃圾，做好垃圾分类，保持楼道、电梯等公共区域整洁。");
        keywordResponses.put("噪音", "根据公共秩序维护公约，居民应在22:00-8:00保持安静，不制造噪音，避免影响他人休息。");
        keywordResponses.put("停车", "根据公共秩序维护公约，车辆应有序停放，不得占用消防通道，确保安全通道畅通。");
        keywordResponses.put("宠物", "根据文明行为规范公约，遛狗时应清理宠物粪便，保持公共环境卫生。");
        keywordResponses.put("邻里", "根据邻里和睦相处公约，邻里之间应相互尊重，和睦相处，遇到纠纷时理性沟通，寻求和平解决。");
        keywordResponses.put("绿化", "根据社区环境维护公约，居民应爱护绿化，不践踏草坪，共同维护社区环境。");
        keywordResponses.put("门禁", "根据公共秩序维护公约，居民应遵守门禁制度，配合安保工作，维护社区安全。");
        keywordResponses.put("公共设施", "根据公共秩序维护公约，居民应爱护公共设施，不故意损坏，共同维护社区财产。");
        
        // 检查关键词匹配
        for (Map.Entry<String, String> entry : keywordResponses.entrySet()) {
            if (lowerQuestion.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 模糊匹配公约内容
        for (CommunityAgreement agreement : agreements) {
            String content = agreement.getContent().toLowerCase();
            if (containsSimilarContent(lowerQuestion, content)) {
                return String.format("根据《%s》相关规定：%s", 
                    agreement.getTitle(), 
                    extractRelevantContent(agreement.getContent(), question));
            }
        }
        
        // 默认回答
        return "您好！我是社区公约智能助手。请您具体描述遇到的问题，我会根据社区公约为您提供相应的解答。您可以询问关于垃圾分类、噪音管理、停车规范、邻里关系等方面的问题。";
    }

    /**
     * 检查是否包含相似内容
     */
    private boolean containsSimilarContent(String question, String content) {
        String[] questionWords = question.split("[\\s，。！？、]+");
        for (String word : questionWords) {
            if (word.length() > 1 && content.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取相关内容
     */
    private String extractRelevantContent(String content, String question) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (containsSimilarContent(question.toLowerCase(), line.toLowerCase())) {
                return line.trim();
            }
        }
        return lines.length > 0 ? lines[0].trim() : content;
    }

    /**
     * 清除公约相关缓存
     */
    private void clearAgreementCache() {
        redisTemplate.delete("agreements:active");
    }
    
    /**
     * 从所有用户的学习进度中清除指定公约ID
     */
    private void clearAgreementFromLearningProgress(Integer agreementId) {
        try {
            // 只清理Set类型的学习进度key（progress和complete）
            Set<String> progressKeys = redisTemplate.keys("learning:progress:*");
            if (progressKeys != null && !progressKeys.isEmpty()) {
                for (String key : progressKeys) {
                    redisTemplate.opsForSet().remove(key, agreementId);
                }
                log.info("已清理公约ID {} 的查看进度数据，共处理 {} 个用户", agreementId, progressKeys.size());
            }
            
            Set<String> completeKeys = redisTemplate.keys("learning:complete:*");
            if (completeKeys != null && !completeKeys.isEmpty()) {
                for (String key : completeKeys) {
                    redisTemplate.opsForSet().remove(key, agreementId);
                }
                log.info("已清理公约ID {} 的完成进度数据，共处理 {} 个用户", agreementId, completeKeys.size());
            }
            
            // 清理所有用户的学习统计缓存，强制重新计算
            Set<String> statsKeys = redisTemplate.keys("learning:stats:*");
            if (statsKeys != null && !statsKeys.isEmpty()) {
                redisTemplate.delete(statsKeys);
                log.info("已清理学习统计缓存，共清理 {} 个缓存", statsKeys.size());
            }
            
            log.info("已清理公约ID {} 的所有学习进度数据", agreementId);
        } catch (Exception e) {
            log.error("清理学习进度数据失败", e);
        }
    }
    
    /**
     * 清理所有用户的学习统计缓存
     */
    private void clearAllLearningStatsCache() {
        try {
            // 清理所有用户的学习统计缓存，强制重新计算
            Set<String> statsKeys = redisTemplate.keys("learning:stats:*");
            if (statsKeys != null && !statsKeys.isEmpty()) {
                redisTemplate.delete(statsKeys);
                log.info("已清理所有学习统计缓存，共清理 {} 个缓存", statsKeys.size());
            }
        } catch (Exception e) {
            log.error("清理学习统计缓存失败", e);
        }
    }
} 