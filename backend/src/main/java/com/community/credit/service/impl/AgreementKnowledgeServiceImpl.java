package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.entity.AgreementKnowledge;
import com.community.credit.entity.CommunityAgreement;
import com.community.credit.mapper.AgreementKnowledgeMapper;
import com.community.credit.mapper.CommunityAgreementMapper;
import com.community.credit.service.AgreementKnowledgeService;
import com.community.credit.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 公约知识库服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class AgreementKnowledgeServiceImpl extends ServiceImpl<AgreementKnowledgeMapper, AgreementKnowledge> 
    implements AgreementKnowledgeService {

    @Autowired
    private CommunityAgreementMapper communityAgreementMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    @Override
    public void createOrUpdateKnowledge(CommunityAgreement agreement) {
        // 只处理文本类型的公约，视频类型不加入知识库
        log.info("处理公约：ID={}, 标题={}, 类型={}, 状态={}", 
            agreement.getId(), agreement.getTitle(), agreement.getContentType(), agreement.getIsActive());
        
        if (agreement.getContentType() != CommunityAgreement.ContentType.TEXT || !agreement.getIsActive()) {
            log.info("跳过公约：ID={}, 原因：类型不是TEXT或未启用", agreement.getId());
            return;
        }

        try {
            // 检查是否已存在
            QueryWrapper<AgreementKnowledge> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("agreement_id", agreement.getId());
            AgreementKnowledge existingKnowledge = this.getOne(queryWrapper);

            if (existingKnowledge != null) {
                // 更新现有记录 - 只更新核心字段
                existingKnowledge.setTitle(agreement.getTitle());
                existingKnowledge.setContent(agreement.getContent());
                existingKnowledge.setIsActive(agreement.getIsActive());
                existingKnowledge.setUpdatedTime(LocalDateTime.now());
                this.updateById(existingKnowledge);
                log.info("更新知识库记录，公约ID: {}", agreement.getId());
            } else {
                // 创建新记录 - 只保存核心字段
                AgreementKnowledge knowledge = new AgreementKnowledge();
                knowledge.setAgreementId(agreement.getId());
                knowledge.setTitle(agreement.getTitle());
                knowledge.setContent(agreement.getContent());
                knowledge.setIsActive(agreement.getIsActive());
                knowledge.setCreatedTime(LocalDateTime.now());
                knowledge.setUpdatedTime(LocalDateTime.now());
                this.save(knowledge);
                log.info("创建知识库记录，公约ID: {}", agreement.getId());
            }
        } catch (Exception e) {
            log.error("处理知识库记录失败，公约ID: {}", agreement.getId(), e);
        }
    }

    @Override
    public void deleteByAgreementId(Integer agreementId) {
        try {
            QueryWrapper<AgreementKnowledge> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("agreement_id", agreementId);
            this.remove(queryWrapper);
            log.info("删除知识库记录，公约ID: {}", agreementId);
        } catch (Exception e) {
            log.error("删除知识库记录失败，公约ID: {}", agreementId, e);
        }
    }

    @Override
    public List<AgreementKnowledge> searchRelevantKnowledge(String question) {
        if (!StringUtils.hasText(question)) {
            return new ArrayList<>();
        }

        try {
            // 直接在content字段中搜索问题关键词
            QueryWrapper<AgreementKnowledge> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("is_active", true);
            
            // 提取问题关键词并在content中搜索
            String[] keywords = extractQuestionKeywords(question);
            for (String keyword : keywords) {
                if (StringUtils.hasText(keyword) && keyword.length() > 1) {
                    queryWrapper.or().like("content", keyword);
                    queryWrapper.or().like("title", keyword);
                }
            }
            
            List<AgreementKnowledge> results = this.list(queryWrapper);
            
            // 按相关性排序（简单的关键词匹配计分）
            return results.stream()
                .sorted((k1, k2) -> calculateRelevanceScore(k2, question) - calculateRelevanceScore(k1, question))
                .limit(5) // 最多返回5条相关记录
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("搜索相关知识库内容失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> answerQuestion(String question) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 搜索相关知识库内容
            List<AgreementKnowledge> relevantKnowledge = searchRelevantKnowledge(question);
            
            if (relevantKnowledge.isEmpty()) {
                result.put("answer", "抱歉，我在社区公约中没有找到与您问题相关的内容。请您重新描述问题或联系社区管理员。");
                result.put("source", "系统回复");
                result.put("confidence", 0.0);
                return result;
            }

            // 构建知识库上下文
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("根据以下社区公约内容回答用户问题：\n\n");
            
            for (int i = 0; i < relevantKnowledge.size(); i++) {
                AgreementKnowledge knowledge = relevantKnowledge.get(i);
                contextBuilder.append(String.format("公约%d：%s\n", i + 1, knowledge.getTitle()));
                contextBuilder.append(String.format("内容：%s\n\n", knowledge.getContent()));
            }
            
            contextBuilder.append(String.format("用户问题：%s\n\n", question));
            contextBuilder.append("请基于上述公约内容，用简洁明了的语言回答用户问题。如果公约中没有直接相关的内容，请说明并建议用户联系社区管理员。");

            // 调用AI服务生成回答
            String aiAnswer = deepSeekService.askQuestion(question, contextBuilder.toString());
            
            if (StringUtils.hasText(aiAnswer)) {
                result.put("answer", aiAnswer);
                result.put("source", "AI智能回答（基于社区公约）");
                result.put("confidence", 0.8);
                result.put("relatedAgreements", relevantKnowledge.stream()
                    .map(k -> Map.of(
                        "id", k.getAgreementId(),
                        "title", k.getTitle(),
                        "content", k.getContent().length() > 100 ? k.getContent().substring(0, 100) + "..." : k.getContent()
                    ))
                    .collect(Collectors.toList()));
            } else {
                result.put("answer", "抱歉，AI服务暂时不可用。请稍后再试或联系社区管理员。");
                result.put("source", "系统回复");
                result.put("confidence", 0.0);
            }
            
        } catch (Exception e) {
            log.error("AI问答失败", e);
            result.put("answer", "抱歉，处理您的问题时出现了错误。请稍后再试或联系社区管理员。");
            result.put("source", "系统回复");
            result.put("confidence", 0.0);
        }
        
        return result;
    }

    @Override
    public String extractKeywords(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        try {
            // 简单的关键词提取逻辑
            // 移除HTML标签
            String cleanContent = content.replaceAll("<[^>]+>", "");
            
            // 分词并过滤停用词
            String[] words = cleanContent.split("[\\s\\p{Punct}]+");
            Set<String> keywords = new HashSet<>();
            
            for (String word : words) {
                if (StringUtils.hasText(word) && word.length() >= 2 && !isStopWord(word)) {
                    keywords.add(word);
                }
            }
            
            return keywords.stream()
                .limit(20) // 最多20个关键词
                .collect(Collectors.joining(","));
                
        } catch (Exception e) {
            log.error("提取关键词失败", e);
            return "";
        }
    }

    @Override
    public String generateSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        try {
            // 移除HTML标签
            String cleanContent = content.replaceAll("<[^>]+>", "");
            
            // 简单的摘要生成：取前200个字符
            if (cleanContent.length() <= 200) {
                return cleanContent;
            } else {
                return cleanContent.substring(0, 200) + "...";
            }
            
        } catch (Exception e) {
            log.error("生成摘要失败", e);
            return content.length() > 200 ? content.substring(0, 200) + "..." : content;
        }
    }

    @Override
    public void initializeKnowledgeBase() {
        try {
            log.info("开始初始化知识库...");
            
            // 清空现有知识库
            this.remove(new QueryWrapper<>());
            
            // 获取所有启用的文本类型公约
            List<CommunityAgreement> agreements = communityAgreementMapper.getActiveAgreements();
            
            int count = 0;
            for (CommunityAgreement agreement : agreements) {
                    createOrUpdateKnowledge(agreement);
                    count++;
            }
            
            log.info("知识库初始化完成，共处理 {} 条公约记录", count);
            
        } catch (Exception e) {
            log.error("初始化知识库失败", e);
        }
    }

    /**
     * 提取问题关键词
     */
    private String[] extractQuestionKeywords(String question) {
        // 简单的问题关键词提取
        String cleanQuestion = question.replaceAll("[\\p{Punct}]+", " ");
        return cleanQuestion.split("\\s+");
    }

    /**
     * 计算相关性得分
     */
    private int calculateRelevanceScore(AgreementKnowledge knowledge, String question) {
        int score = 0;
        String[] questionWords = question.toLowerCase().split("\\s+");
        String knowledgeText = (knowledge.getTitle() + " " + knowledge.getContent()).toLowerCase();
        
        for (String word : questionWords) {
            if (StringUtils.hasText(word) && knowledgeText.contains(word)) {
                score++;
            }
        }
        
        return score;
    }

    /**
     * 判断是否为停用词
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"
        );
        return stopWords.contains(word.toLowerCase());
    }
} 