package com.community.credit.service.impl;

import com.community.credit.service.DeepSeekService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DeepSeekServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String askQuestion(String question, String context) {
        if (!enabled || apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("DeepSeek服务未启用或API Key未配置，使用默认回答");
            return getDefaultAnswer(question);
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(question, context);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            } else {
                log.error("DeepSeek API调用失败，状态码: {}", response.getStatusCode());
                return getDefaultAnswer(question);
            }
            
        } catch (Exception e) {
            log.error("调用DeepSeek API异常", e);
            return getDefaultAnswer(question);
        }
    }

    @Override
    public boolean isServiceAvailable() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(String question, String context) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);
        
        // 构建消息列表
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt(context));
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        
        requestBody.put("messages", List.of(systemMessage, userMessage));
        
        return requestBody;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String context) {
        return String.format(
            "你是一个社区居民公约智能助手，专门解答关于社区管理规定的问题。" +
            "请基于以下社区公约内容回答用户问题，回答要准确、简洁、实用。" +
            "如果问题超出公约范围，请礼貌地说明并引导用户提问相关内容。\n\n" +
            "社区公约内容：\n%s\n\n" +
            "请用中文回答，语气友善专业。", 
            context != null ? context : "暂无具体公约内容"
        );
    }

    /**
     * 解析API响应
     */
    private String parseResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode choices = jsonNode.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText().trim();
                    }
                }
            }
            
            log.warn("DeepSeek API响应格式异常: {}", responseBody);
            return "抱歉，AI服务暂时无法正常回答，请稍后重试。";
            
        } catch (Exception e) {
            log.error("解析DeepSeek API响应失败", e);
            return "抱歉，处理回答时出现错误，请稍后重试。";
        }
    }

    /**
     * 获取默认回答（当API不可用时）
     */
    private String getDefaultAnswer(String question) {
        String lowerQuestion = question.toLowerCase();
        
        // 简单的关键词匹配
        if (lowerQuestion.contains("垃圾")) {
            return "根据社区环境维护公约，居民应当不在公共区域乱丢垃圾，做好垃圾分类，保持楼道、电梯等公共区域整洁。";
        } else if (lowerQuestion.contains("噪音")) {
            return "根据公共秩序维护公约，居民应在22:00-8:00保持安静，不制造噪音，避免影响他人休息。";
        } else if (lowerQuestion.contains("停车")) {
            return "根据公共秩序维护公约，车辆应有序停放，不得占用消防通道，确保安全通道畅通。";
        } else if (lowerQuestion.contains("宠物")) {
            return "根据文明行为规范公约，遛狗时应清理宠物粪便，保持公共环境卫生。";
        } else if (lowerQuestion.contains("邻里")) {
            return "根据邻里和睦相处公约，邻里之间应相互尊重，和睦相处，遇到纠纷时理性沟通，寻求和平解决。";
        }
        
        return "您好！我是社区公约智能助手。请您具体描述遇到的问题，我会根据社区公约为您提供相应的解答。您可以询问关于垃圾分类、噪音管理、停车规范、邻里关系等方面的问题。";
    }
} 