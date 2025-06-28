package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.dto.AgreementQuestionRequest;
import com.community.credit.dto.AgreementRequest;
import com.community.credit.entity.CommunityAgreement;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
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

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 社区公约控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/agreements")
@Tag(name = "社区公约管理", description = "社区公约学习和管理接口")
public class CommunityAgreementController {

    @Autowired
    private CommunityAgreementService agreementService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ==================== 居民端接口 ====================

    @GetMapping("/list")
    @Operation(summary = "获取公约列表", description = "获取所有启用的社区公约")
    // @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN}) // 临时注释掉权限检查
    public Result<List<CommunityAgreement>> getAgreements() {
        List<CommunityAgreement> agreements = agreementService.getActiveAgreements();
        return Result.success(agreements);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公约详情", description = "根据ID获取具体公约内容")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<CommunityAgreement> getAgreementDetail(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            Authentication authentication) {
        
        CommunityAgreement agreement = agreementService.getById(id);
        if (agreement == null || !agreement.getIsActive()) {
            return Result.error("公约不存在或已禁用");
        }

        // 记录学习进度
        if (authentication != null) {
            String username = authentication.getName();
            User currentUser = userService.getByUsername(username);
            if (currentUser != null) {
                agreementService.recordLearningProgress(currentUser.getId(), id, "view");
            }
        }

        return Result.success(agreement);
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

        Map<String, Object> result = agreementService.askQuestion(request);
        return Result.success(result);
    }

    @GetMapping("/learning/stats")
    @Operation(summary = "获取学习统计", description = "获取当前用户的学习进度统计")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Map<String, Object>> getLearningStats(
            @Parameter(description = "用户ID") @RequestParam(defaultValue = "1") Integer userId) {
        
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> stats = agreementService.getLearningStats(currentUser.getId());
        return Result.success(stats);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "标记完成学习", description = "标记用户已完成某个公约的学习")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<Void> markComplete(
            @Parameter(description = "公约ID") @PathVariable Integer id,
            Authentication authentication) {
        
        if (authentication == null) {
            return Result.error("请先登录");
        }

        String username = authentication.getName();
        User currentUser = userService.getByUsername(username);
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

    @PostMapping("/admin/clear-cache")
    @Operation(summary = "清理缓存", description = "清理Redis中的公约缓存（临时修复接口）")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> clearCache() {
        try {
            // 清理所有agreement相关的缓存
            redisTemplate.delete("agreement:*");
            return Result.success("缓存清理成功");
        } catch (Exception e) {
            log.error("清理缓存失败", e);
            return Result.error("缓存清理失败: " + e.getMessage());
        }
    }
} 