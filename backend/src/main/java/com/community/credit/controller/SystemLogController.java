package com.community.credit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.credit.common.Result;
import com.community.credit.entity.SystemLog;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统日志管理控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "系统日志管理", description = "系统操作日志查询和管理接口")
@RestController
@RequestMapping("/system/logs")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    @Operation(summary = "获取系统日志列表", description = "分页查询系统操作日志")
    @GetMapping("/list")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<IPage<SystemLog>> getSystemLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "用户ID") @RequestParam(required = false) Integer userId,
            @Parameter(description = "操作类型") @RequestParam(required = false) String operationType,
            @Parameter(description = "执行状态") @RequestParam(required = false) SystemLog.LogStatus status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime) {
        
        Page<SystemLog> pageParam = new Page<>(page, size);
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        
        // 添加查询条件
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.hasText(operationType)) {
            queryWrapper.like("operation_type", operationType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(startTime)) {
            queryWrapper.ge("created_time", startTime);
        }
        if (StringUtils.hasText(endTime)) {
            queryWrapper.le("created_time", endTime);
        }
        
        // 按时间倒序排列
        queryWrapper.orderByDesc("created_time");
        
        IPage<SystemLog> result = systemLogService.page(pageParam, queryWrapper);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "获取日志详情", description = "根据ID获取系统日志详细信息")
    @GetMapping("/{id}")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<SystemLog> getLogDetail(@Parameter(description = "日志ID") @PathVariable Integer id) {
        SystemLog log = systemLogService.getById(id);
        if (log == null) {
            return Result.error("日志记录不存在");
        }
        return Result.success("获取成功", log);
    }

    @Operation(summary = "获取日志统计", description = "获取系统日志统计信息")
    @GetMapping("/statistics")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getLogStatistics(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总日志数
        long totalLogs = systemLogService.count();
        stats.put("totalLogs", totalLogs);
        
        // 最近指定天数的日志数
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        QueryWrapper<SystemLog> recentQuery = new QueryWrapper<>();
        recentQuery.ge("created_time", startTime);
        long recentLogs = systemLogService.count(recentQuery);
        stats.put("recentLogs", recentLogs);
        
        // 成功日志数
        QueryWrapper<SystemLog> successQuery = new QueryWrapper<>();
        successQuery.eq("status", SystemLog.LogStatus.SUCCESS);
        long successLogs = systemLogService.count(successQuery);
        stats.put("successLogs", successLogs);
        
        // 失败日志数
        QueryWrapper<SystemLog> failedQuery = new QueryWrapper<>();
        failedQuery.eq("status", SystemLog.LogStatus.FAILED);
        long failedLogs = systemLogService.count(failedQuery);
        stats.put("failedLogs", failedLogs);
        
        // 错误日志数
        QueryWrapper<SystemLog> errorQuery = new QueryWrapper<>();
        errorQuery.eq("status", SystemLog.LogStatus.ERROR);
        long errorLogs = systemLogService.count(errorQuery);
        stats.put("errorLogs", errorLogs);
        
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "清理历史日志", description = "清理指定天数之前的历史日志")
    @DeleteMapping("/clean")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<String> cleanHistoryLogs(
            @Parameter(description = "保留天数") @RequestParam(defaultValue = "30") Integer keepDays) {
        
        if (keepDays < 7) {
            return Result.error("保留天数不能少于7天");
        }
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(keepDays);
        QueryWrapper<SystemLog> deleteQuery = new QueryWrapper<>();
        deleteQuery.lt("created_time", cutoffTime);
        
        long deletedCount = systemLogService.count(deleteQuery);
        boolean success = systemLogService.remove(deleteQuery);
        
        if (success) {
            return Result.success("清理完成，删除了 " + deletedCount + " 条历史日志");
        } else {
            return Result.error("清理失败");
        }
    }

    @Operation(summary = "获取操作类型统计", description = "获取各操作类型的统计信息")
    @GetMapping("/operation-types")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getOperationTypeStats() {
        // 这里可以通过自定义SQL查询来获取操作类型统计
        // 简化实现，返回基本信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "操作类型统计功能待完善");
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "获取用户操作统计", description = "获取用户操作频次统计")
    @GetMapping("/user-operations")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getUserOperationStats(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "7") Integer days) {
        
        // 这里可以通过自定义SQL查询来获取用户操作统计
        // 简化实现，返回基本信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("message", "用户操作统计功能待完善");
        return Result.success("获取成功", stats);
    }
} 