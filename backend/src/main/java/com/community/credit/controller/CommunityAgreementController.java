package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.dto.AgreementQuestionRequest;
import com.community.credit.dto.AgreementRequest;
import com.community.credit.entity.CommunityAgreement;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.AgreementKnowledgeService;
import com.community.credit.service.CommunityAgreementService;
import com.community.credit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 社区公约控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/agreements")
@Tag(name = "社区公约管理", description = "社区公约学习和管理接口")
public class CommunityAgreementController {

    @Autowired
    private CommunityAgreementService agreementService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AgreementKnowledgeService agreementKnowledgeService;

    // ==================== 居民端接口 ====================

    @GetMapping("/list")
    @Operation(summary = "获取公约列表", description = "获取所有启用的社区公约")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<List<CommunityAgreement>> getAgreements() {
        List<CommunityAgreement> agreements = agreementService.getActiveAgreements();
        return Result.success(agreements);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公约详情", description = "根据ID获取具体公约内容")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Map<String, Object>> getAgreementDetail(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            HttpServletRequest request) {
        
        CommunityAgreement agreement = agreementService.getById(id);
        if (agreement == null || !agreement.getIsActive()) {
            return Result.error("公约不存在或已禁用");
        }

        // 获取用户信息
        Integer currentUserId = getCurrentUserId(request);
        boolean isCompleted = false;
        
        if (currentUserId != null) {
            // 记录学习进度
            agreementService.recordLearningProgress(currentUserId, id, "view");
            
            // 检查是否已完成学习
            String completeKey = "learning:complete:" + currentUserId;
            isCompleted = redisTemplate.opsForSet().isMember(completeKey, id);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("agreement", agreement);
        result.put("isCompleted", isCompleted);
        
        return Result.success(result);
    }

    @PostMapping("/ask")
    @Operation(summary = "AI智能问答", description = "基于公约内容的智能问答")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Map<String, Object>> askQuestion(
            @Valid @RequestBody AgreementQuestionRequest request,
            Authentication authentication) {
        
        // 记录问答行为
        if (authentication != null) {
            String username = authentication.getName();
            User currentUser = userService.getByUsername(username);
            if (currentUser != null) {
                agreementService.recordLearningProgress(currentUser.getId(), request.getAgreementId(), "question");
            }
        }

        // 使用新的知识库服务进行AI问答
        Map<String, Object> result = agreementKnowledgeService.answerQuestion(request.getQuestion());
        result.put("sessionId", request.getSessionId() != null ? request.getSessionId() : java.util.UUID.randomUUID().toString());
        
        return Result.success(result);
    }

    @GetMapping("/learning/stats")
    @Operation(summary = "获取学习统计", description = "获取当前用户的学习进度统计")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Map<String, Object>> getLearningStats(HttpServletRequest request) {
        
        Integer currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> stats = agreementService.getLearningStats(currentUser.getId());
        return Result.success(stats);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "获取公约学习状态", description = "获取用户对特定公约的学习状态")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Map<String, Object>> getAgreementLearningStatus(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            HttpServletRequest request) {
        
        Integer currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 检查公约是否存在且启用
        CommunityAgreement agreement = agreementService.getById(id);
        if (agreement == null || !agreement.getIsActive()) {
            return Result.error("公约不存在或已禁用");
        }

        // 检查学习状态
        String progressKey = "learning:progress:" + currentUserId;
        String completeKey = "learning:complete:" + currentUserId;
        
        boolean isViewed = redisTemplate.opsForSet().isMember(progressKey, id);
        boolean isCompleted = redisTemplate.opsForSet().isMember(completeKey, id);

        Map<String, Object> result = new HashMap<>();
        result.put("isViewed", isViewed);
        result.put("isCompleted", isCompleted);
        
        return Result.success(result);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "标记完成学习", description = "标记用户已完成某个公约的学习")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Void> markComplete(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            HttpServletRequest request) {
        
        Integer currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        User currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        // 检查公约是否存在
        CommunityAgreement agreement = agreementService.getById(id);
        if (agreement == null || !agreement.getIsActive()) {
            return Result.error("公约不存在或已禁用");
        }

        agreementService.recordLearningProgress(currentUser.getId(), id, "complete");
        return Result.success();
    }

    // ==================== 管理员端接口 ====================

    @GetMapping("/admin/list")
    @Operation(summary = "管理员获取公约列表", description = "获取所有公约（包括禁用的）")
    @RequireRole({User.UserRole.ADMIN})
    public Result<List<CommunityAgreement>> getAllAgreements() {
        List<CommunityAgreement> agreements = agreementService.list();
        return Result.success(agreements);
    }

    @PostMapping("/admin/create")
    @Operation(summary = "创建公约", description = "创建新的社区公约")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Integer> createAgreement(@Valid @RequestBody AgreementRequest request) {
        Integer agreementId = agreementService.createAgreement(request);
        return Result.success(agreementId);
    }

    @PutMapping("/admin/{id}")
    @Operation(summary = "更新公约", description = "更新指定公约的内容")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Void> updateAgreement(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            @Valid @RequestBody AgreementRequest request) {
        
        agreementService.updateAgreement(id, request);
        return Result.success();
    }

    @DeleteMapping("/admin/{id}")
    @Operation(summary = "删除公约", description = "删除指定的公约")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Void> deleteAgreement(@Parameter(description = "公约ID") @PathVariable Integer id) {
        agreementService.deleteAgreement(id);
        return Result.success();
    }

    @PutMapping("/admin/{id}/status")
    @Operation(summary = "切换公约状态", description = "启用或禁用指定公约")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Void> toggleAgreementStatus(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            @Parameter(description = "是否启用") @RequestParam Boolean isActive) {
        
        agreementService.toggleAgreementStatus(id, isActive);
        return Result.success();
    }

    @PutMapping("/admin/{id}/order")
    @Operation(summary = "调整公约排序", description = "调整公约的显示顺序")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Void> updateAgreementOrder(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            @Parameter(description = "新的排序号") @RequestParam Integer orderNum) {
        
        agreementService.updateAgreementOrder(id, orderNum);
        return Result.success();
    }

    @GetMapping("/admin/stats")
    @Operation(summary = "获取公约统计", description = "获取公约学习的整体统计数据")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Map<String, Object>> getAgreementStats() {
        // 这里可以添加更多统计逻辑
        Map<String, Object> stats = Map.of(
            "totalAgreements", agreementService.count(),
            "activeAgreements", agreementService.getActiveAgreements().size()
        );
        return Result.success(stats);
    }

    @PostMapping("/admin/init-knowledge")
    @Operation(summary = "初始化知识库", description = "将现有公约导入AI知识库")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> initializeKnowledgeBase() {
        try {
            agreementKnowledgeService.initializeKnowledgeBase();
            return Result.success("知识库初始化成功");
        } catch (Exception e) {
            log.error("初始化知识库失败", e);
            return Result.error("初始化知识库失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/clear-cache")
    @Operation(summary = "清理缓存", description = "清理Redis中的公约和学习相关缓存（临时修复接口）")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> clearCache() {
        try {
            // 清理所有agreement相关的缓存
            Set<String> keys = redisTemplate.keys("agreement:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            // 清理所有学习相关的缓存
            Set<String> learningKeys = redisTemplate.keys("learning:*");
            if (learningKeys != null && !learningKeys.isEmpty()) {
                redisTemplate.delete(learningKeys);
            }
            
            log.info("清理了 {} 个agreement缓存和 {} 个learning缓存", 
                keys != null ? keys.size() : 0, 
                learningKeys != null ? learningKeys.size() : 0);
            
            return Result.success("缓存清理成功，请重新登录后测试学习状态");
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return Result.error("缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID - 支持Token和Session两种方式
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        // 1. 优先尝试从Token获取（Authorization header）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 简单的token解析 - 假设token格式为 "userId:timestamp:signature"
                String[] parts = token.split(":");
                if (parts.length >= 1) {
                    return Integer.valueOf(parts[0]);
                }
            } catch (Exception e) {
                log.debug("Token解析失败: {}", e.getMessage());
            }
        }
        
        // 2. 尝试从URL参数获取token
        String tokenParam = request.getParameter("token");
        if (tokenParam != null) {
            try {
                String[] parts = tokenParam.split(":");
                if (parts.length >= 1) {
                    return Integer.valueOf(parts[0]);
                }
            } catch (Exception e) {
                log.debug("URL Token解析失败: {}", e.getMessage());
            }
        }
        
        // 3. 回退到Session方式
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Integer) session.getAttribute("userId");
        }
        
        return null;
    }
} 