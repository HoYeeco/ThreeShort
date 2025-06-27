package com.community.credit;

import com.community.credit.dto.AgreementQuestionRequest;
import com.community.credit.dto.AgreementRequest;
import com.community.credit.entity.CommunityAgreement;
import com.community.credit.service.CommunityAgreementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 社区公约服务测试类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommunityAgreementServiceTest {

    @Autowired
    private CommunityAgreementService agreementService;

    @Test
    public void testCreateAgreement() {
        // 准备测试数据
        AgreementRequest request = new AgreementRequest();
        request.setTitle("测试公约");
        request.setContent("这是一个测试公约内容，用于验证公约创建功能。");
        request.setContentType(CommunityAgreement.ContentType.TEXT);
        request.setOrderNum(1);
        request.setIsActive(true);

        // 创建公约
        Integer agreementId = agreementService.createAgreement(request);

        // 验证结果
        assertNotNull(agreementId);
        
        CommunityAgreement agreement = agreementService.getById(agreementId);
        assertNotNull(agreement);
        assertEquals("测试公约", agreement.getTitle());
        assertEquals("这是一个测试公约内容，用于验证公约创建功能。", agreement.getContent());
        assertEquals(CommunityAgreement.ContentType.TEXT, agreement.getContentType());
        assertTrue(agreement.getIsActive());
    }

    @Test
    public void testGetActiveAgreements() {
        // 创建测试公约
        AgreementRequest request1 = new AgreementRequest();
        request1.setTitle("启用公约1");
        request1.setContent("启用的公约内容1");
        request1.setContentType(CommunityAgreement.ContentType.TEXT);
        request1.setOrderNum(1);
        request1.setIsActive(true);

        AgreementRequest request2 = new AgreementRequest();
        request2.setTitle("禁用公约2");
        request2.setContent("禁用的公约内容2");
        request2.setContentType(CommunityAgreement.ContentType.TEXT);
        request2.setOrderNum(2);
        request2.setIsActive(false);

        agreementService.createAgreement(request1);
        Integer agreementId2 = agreementService.createAgreement(request2);

        // 获取启用的公约
        List<CommunityAgreement> activeAgreements = agreementService.getActiveAgreements();

        // 验证结果 - 应该只包含启用的公约
        assertTrue(activeAgreements.stream().allMatch(CommunityAgreement::getIsActive));
    }

    @Test
    public void testAIQuestionAnswer() {
        // 创建测试公约
        AgreementRequest request = new AgreementRequest();
        request.setTitle("垃圾分类公约");
        request.setContent("居民应当按照规定进行垃圾分类，不得在公共区域乱丢垃圾。");
        request.setContentType(CommunityAgreement.ContentType.TEXT);
        request.setOrderNum(1);
        request.setIsActive(true);

        Integer agreementId = agreementService.createAgreement(request);

        // 测试AI问答
        AgreementQuestionRequest questionRequest = new AgreementQuestionRequest();
        questionRequest.setQuestion("如何处理垃圾？");
        questionRequest.setAgreementId(agreementId);

        Map<String, Object> result = agreementService.askQuestion(questionRequest);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("question"));
        assertTrue(result.containsKey("answer"));
        assertTrue(result.containsKey("timestamp"));
        assertEquals("如何处理垃圾？", result.get("question"));
        
        String answer = (String) result.get("answer");
        assertNotNull(answer);
        assertTrue(answer.contains("垃圾") || answer.contains("分类"));
    }

    @Test
    public void testAIQuestionWithKeywords() {
        // 测试关键词匹配
        AgreementQuestionRequest request = new AgreementQuestionRequest();
        request.setQuestion("邻里之间发生纠纷怎么办？");

        Map<String, Object> result = agreementService.askQuestion(request);

        // 验证结果
        assertNotNull(result);
        String answer = (String) result.get("answer");
        assertTrue(answer.contains("邻里") || answer.contains("和睦") || answer.contains("沟通"));
    }

    @Test
    public void testLearningProgressTracking() {
        // 创建测试公约
        AgreementRequest request = new AgreementRequest();
        request.setTitle("测试公约");
        request.setContent("测试内容");
        request.setContentType(CommunityAgreement.ContentType.TEXT);
        request.setOrderNum(1);
        request.setIsActive(true);

        Integer agreementId = agreementService.createAgreement(request);
        Integer userId = 1;

        // 记录学习进度
        agreementService.recordLearningProgress(userId, agreementId, "view");
        agreementService.recordLearningProgress(userId, agreementId, "question");
        agreementService.recordLearningProgress(userId, agreementId, "complete");

        // 获取学习统计
        Map<String, Object> stats = agreementService.getLearningStats(userId);

        // 验证结果
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalAgreements"));
        assertTrue(stats.containsKey("viewedAgreements"));
        assertTrue(stats.containsKey("completionRate"));
        assertTrue(stats.containsKey("questionCount"));

        // 验证问答次数被正确记录
        Integer questionCount = (Integer) stats.get("questionCount");
        assertTrue(questionCount >= 1);
    }

    @Test
    public void testUpdateAgreement() {
        // 创建公约
        AgreementRequest createRequest = new AgreementRequest();
        createRequest.setTitle("原始标题");
        createRequest.setContent("原始内容");
        createRequest.setContentType(CommunityAgreement.ContentType.TEXT);
        createRequest.setOrderNum(1);
        createRequest.setIsActive(true);

        Integer agreementId = agreementService.createAgreement(createRequest);

        // 更新公约
        AgreementRequest updateRequest = new AgreementRequest();
        updateRequest.setTitle("更新后标题");
        updateRequest.setContent("更新后内容");
        updateRequest.setContentType(CommunityAgreement.ContentType.TEXT);
        updateRequest.setOrderNum(2);
        updateRequest.setIsActive(false);

        agreementService.updateAgreement(agreementId, updateRequest);

        // 验证更新结果
        CommunityAgreement updated = agreementService.getById(agreementId);
        assertEquals("更新后标题", updated.getTitle());
        assertEquals("更新后内容", updated.getContent());
        assertEquals(Integer.valueOf(2), updated.getOrderNum());
        assertFalse(updated.getIsActive());
    }

    @Test
    public void testToggleAgreementStatus() {
        // 创建启用的公约
        AgreementRequest request = new AgreementRequest();
        request.setTitle("测试公约");
        request.setContent("测试内容");
        request.setContentType(CommunityAgreement.ContentType.TEXT);
        request.setOrderNum(1);
        request.setIsActive(true);

        Integer agreementId = agreementService.createAgreement(request);

        // 禁用公约
        agreementService.toggleAgreementStatus(agreementId, false);
        CommunityAgreement disabled = agreementService.getById(agreementId);
        assertFalse(disabled.getIsActive());

        // 重新启用公约
        agreementService.toggleAgreementStatus(agreementId, true);
        CommunityAgreement enabled = agreementService.getById(agreementId);
        assertTrue(enabled.getIsActive());
    }

    @Test
    public void testDeleteAgreement() {
        // 创建公约
        AgreementRequest request = new AgreementRequest();
        request.setTitle("待删除公约");
        request.setContent("待删除内容");
        request.setContentType(CommunityAgreement.ContentType.TEXT);
        request.setOrderNum(1);
        request.setIsActive(true);

        Integer agreementId = agreementService.createAgreement(request);

        // 验证公约存在
        assertNotNull(agreementService.getById(agreementId));

        // 删除公约
        agreementService.deleteAgreement(agreementId);

        // 验证公约已删除
        assertNull(agreementService.getById(agreementId));
    }
} 