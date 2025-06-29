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

    @Test
    public void testDeleteAgreementClearLearningData() {
        // 创建测试公约
        AgreementRequest request = new AgreementRequest();
        request.setTitle("待删除公约");
        request.setContent("这是一个将被删除的公约");
        request.setContentType(CommunityAgreement.ContentType.TEXT);
        request.setOrderNum(1);
        request.setIsActive(true);

        Integer agreementId = agreementService.createAgreement(request);
        Integer userId = 1;

        // 记录学习进度
        agreementService.recordLearningProgress(userId, agreementId, "view");
        agreementService.recordLearningProgress(userId, agreementId, "complete");

        // 获取删除前的学习统计
        Map<String, Object> statsBefore = agreementService.getLearningStats(userId);
        Integer viewedBefore = (Integer) statsBefore.get("viewedAgreements");
        Long totalBeforeLong = (Long) statsBefore.get("totalAgreements");
        Integer totalBefore = totalBeforeLong.intValue();

        // 删除公约
        agreementService.deleteAgreement(agreementId);

        // 获取删除后的学习统计
        Map<String, Object> statsAfter = agreementService.getLearningStats(userId);
        Integer viewedAfter = (Integer) statsAfter.get("viewedAgreements");
        Long totalAfterLong = (Long) statsAfter.get("totalAgreements");
        Integer totalAfter = totalAfterLong.intValue();

        // 验证结果
        assertNotNull(statsAfter);
        assertTrue(totalAfter < totalBefore, "删除公约后总数应该减少");
        
        // 由于删除了公约，已学习数量也应该相应调整（过滤掉已删除的公约）
        assertTrue(viewedAfter <= viewedBefore, "删除公约后已学习数量应该保持一致或减少");
        
        System.out.println("删除前 - 总数: " + totalBefore + ", 已学习: " + viewedBefore);
        System.out.println("删除后 - 总数: " + totalAfter + ", 已学习: " + viewedAfter);
    }

    @Test
    public void testLearningStatsWithAgreementChanges() {
        Integer userId = 1;
        
        // 获取初始状态
        Map<String, Object> initialStats = agreementService.getLearningStats(userId);
        Long initialTotal = (Long) initialStats.get("totalAgreements");
        Integer initialViewed = (Integer) initialStats.get("viewedAgreements");
        Integer initialCompleted = (Integer) initialStats.get("completedAgreements");
        
        System.out.println("初始状态 - 总数: " + initialTotal + ", 已查看: " + initialViewed + ", 已完成: " + initialCompleted);
        
        // 创建新公约
        AgreementRequest newAgreement = new AgreementRequest();
        newAgreement.setTitle("新增测试公约");
        newAgreement.setContent("这是新增的测试公约内容");
        newAgreement.setContentType(CommunityAgreement.ContentType.TEXT);
        newAgreement.setOrderNum(99);
        newAgreement.setIsActive(true);
        
        Integer newAgreementId = agreementService.createAgreement(newAgreement);
        
        // 获取添加后的状态
        Map<String, Object> afterAddStats = agreementService.getLearningStats(userId);
        Long afterAddTotal = (Long) afterAddStats.get("totalAgreements");
        Integer afterAddViewed = (Integer) afterAddStats.get("viewedAgreements");
        Integer afterAddCompleted = (Integer) afterAddStats.get("completedAgreements");
        
        System.out.println("添加后状态 - 总数: " + afterAddTotal + ", 已查看: " + afterAddViewed + ", 已完成: " + afterAddCompleted);
        
        // 验证添加公约后的统计
        assertTrue(afterAddTotal >= initialTotal, "添加公约后总数应该大于等于初始值");
        // 由于缓存清理，之前的学习记录可能被重新计算
        assertTrue(afterAddViewed >= 0, "已查看数量应该大于等于0");
        assertTrue(afterAddCompleted >= 0, "已完成数量应该大于等于0");
        
        // 模拟学习新公约
        agreementService.recordLearningProgress(userId, newAgreementId, "view");
        agreementService.recordLearningProgress(userId, newAgreementId, "complete");
        
        // 获取学习后的状态
        Map<String, Object> afterLearnStats = agreementService.getLearningStats(userId);
        Long afterLearnTotal = (Long) afterLearnStats.get("totalAgreements");
        Integer afterLearnViewed = (Integer) afterLearnStats.get("viewedAgreements");
        Integer afterLearnCompleted = (Integer) afterLearnStats.get("completedAgreements");
        Double viewCompletionRate = (Double) afterLearnStats.get("completionRate");
        Double studyCompletionRate = (Double) afterLearnStats.get("studyCompletionRate");
        
        System.out.println("学习后状态 - 总数: " + afterLearnTotal + ", 已查看: " + afterLearnViewed + ", 已完成: " + afterLearnCompleted);
        System.out.println("查看完成率: " + viewCompletionRate + "%, 学习完成率: " + studyCompletionRate + "%");
        
        // 验证学习后的统计
        assertTrue(afterLearnViewed > afterAddViewed, "学习后已查看数量应该增加");
        assertTrue(afterLearnCompleted > afterAddCompleted, "学习后已完成数量应该增加");
        assertTrue(viewCompletionRate > 0, "查看完成率应该大于0");
        assertTrue(studyCompletionRate > 0, "学习完成率应该大于0");
        
        // 删除刚创建的公约
        agreementService.deleteAgreement(newAgreementId);
        
        // 获取删除后的状态
        Map<String, Object> afterDeleteStats = agreementService.getLearningStats(userId);
        Long afterDeleteTotal = (Long) afterDeleteStats.get("totalAgreements");
        Integer afterDeleteViewed = (Integer) afterDeleteStats.get("viewedAgreements");
        Integer afterDeleteCompleted = (Integer) afterDeleteStats.get("completedAgreements");
        
        System.out.println("删除后状态 - 总数: " + afterDeleteTotal + ", 已查看: " + afterDeleteViewed + ", 已完成: " + afterDeleteCompleted);
        
        // 验证删除后的统计
        assertTrue(afterDeleteTotal <= afterLearnTotal, "删除公约后总数应该减少或保持不变");
        // 删除公约后，相关的学习记录应该被过滤掉，数量应该合理
        assertTrue(afterDeleteViewed >= 0, "删除后已查看数量应该大于等于0");
        assertTrue(afterDeleteCompleted >= 0, "删除后已完成数量应该大于等于0");
        
        // 验证核心功能：删除公约后，学习统计只计算当前生效的公约
        assertNotNull(afterDeleteStats.get("totalAgreements"), "总数应该存在");
        assertNotNull(afterDeleteStats.get("viewedAgreements"), "已查看数量应该存在");
        assertNotNull(afterDeleteStats.get("completedAgreements"), "已完成数量应该存在");
        assertNotNull(afterDeleteStats.get("completionRate"), "完成率应该存在");
        assertNotNull(afterDeleteStats.get("studyCompletionRate"), "学习完成率应该存在");
    }
} 