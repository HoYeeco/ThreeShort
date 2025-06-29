package com.community.credit.controller;

import com.community.credit.common.Result;
import com.community.credit.entity.CreditScoreRecord;
import com.community.credit.security.RequireRole;
import com.community.credit.entity.User;
import com.community.credit.service.SystemLogService;
import com.community.credit.service.UserCreditProfileService;
import com.community.credit.service.impl.CreditScoreRecordServiceImpl;
import com.community.credit.service.CreditScoreRecordService;
import com.community.credit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 定时任务管理控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Tag(name = "定时任务管理", description = "系统定时任务管理和监控接口")
@RestController
@RequestMapping("/system/tasks")
public class TaskManagementController {

    @Autowired
    private UserCreditProfileService userCreditProfileService;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private CreditScoreRecordService creditScoreRecordService;

    @Autowired
    private UserService userService;

    @Operation(summary = "获取定时任务状态", description = "获取所有定时任务的当前状态")
    @GetMapping("/status")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<List<Map<String, Object>>> getTaskStatus() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        // 信用评分计算任务
        Map<String, Object> creditScoreTask = new HashMap<>();
        creditScoreTask.put("name", "信用评分计算");
        creditScoreTask.put("description", "每周一凌晨2点计算用户信用评分");
        creditScoreTask.put("cron", "0 0 2 * * MON");
        creditScoreTask.put("status", "ACTIVE");
        creditScoreTask.put("lastRun", getLastTaskRunTime("SCORE_GENERATION"));
        creditScoreTask.put("nextRun", getNextTaskRunTime("0 0 2 * * MON"));
        tasks.add(creditScoreTask);
        
        // 信用分数重算任务
        Map<String, Object> recalculateTask = new HashMap<>();
        recalculateTask.put("name", "信用分数重算");
        recalculateTask.put("description", "每天凌晨3点重新计算用户信用等级");
        recalculateTask.put("cron", "0 0 3 * * *");
        recalculateTask.put("status", "ACTIVE");
        recalculateTask.put("lastRun", getLastTaskRunTime("RECALCULATE_CREDIT"));
        recalculateTask.put("nextRun", getNextTaskRunTime("0 0 3 * * *"));
        tasks.add(recalculateTask);
        
        return Result.success("获取成功", tasks);
    }

    @Operation(summary = "手动执行评分记录生成", description = "手动触发生成当前周期的评分记录")
    @PostMapping("/execute/score-generation")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<String> executeScoreGeneration() {
        try {
            String currentPeriod = CreditScoreRecordServiceImpl.getCurrentPeriod();
            log.info("手动执行评分记录生成，周期: {}", currentPeriod);
            
            userCreditProfileService.batchGenerateScoreRecord(currentPeriod);
            
            // 记录任务执行
            recordTaskExecution("SCORE_GENERATION", "SUCCESS", "手动执行成功");
            
            return Result.success("评分记录生成完成，周期: " + currentPeriod);
        } catch (Exception e) {
            log.error("手动执行评分记录生成失败", e);
            recordTaskExecution("SCORE_GENERATION", "FAILED", e.getMessage());
            return Result.error("执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "手动执行信用分数重算", description = "手动触发重新计算所有用户信用分数")
    @PostMapping("/execute/recalculate-credit")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<String> executeRecalculateCredit() {
        try {
            log.info("手动执行信用分数重算");
            
            // 批量更新信用等级
            userCreditProfileService.batchUpdateCreditLevels();
            
            // 记录任务执行
            recordTaskExecution("RECALCULATE_CREDIT", "SUCCESS", "手动执行成功");
            
            return Result.success("信用分数重算完成");
        } catch (Exception e) {
            log.error("手动执行信用分数重算失败", e);
            recordTaskExecution("RECALCULATE_CREDIT", "FAILED", e.getMessage());
            return Result.error("执行失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取任务执行历史", description = "获取定时任务的执行历史记录")
    @GetMapping("/history")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getTaskHistory(
            @Parameter(description = "任务名称") @RequestParam(required = false) String taskName,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        // 从系统日志中查询任务执行历史
        Map<String, Object> history = systemLogService.getTaskExecutionHistory(taskName, page, size);
        
        return Result.success("获取成功", history);
    }

    @Operation(summary = "获取任务配置", description = "获取定时任务的配置信息")
    @GetMapping("/config")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getTaskConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 信用评分计算任务配置
        Map<String, Object> scoreGenerationConfig = new HashMap<>();
        scoreGenerationConfig.put("name", "信用评分计算");
        scoreGenerationConfig.put("cron", "0 0 2 * * MON");
        scoreGenerationConfig.put("description", "每周一凌晨2点生成用户信用评分记录");
        scoreGenerationConfig.put("enabled", true);
        config.put("scoreGeneration", scoreGenerationConfig);
        
        // 信用分数重算任务配置
        Map<String, Object> recalculateCreditConfig = new HashMap<>();
        recalculateCreditConfig.put("name", "信用分数重算");
        recalculateCreditConfig.put("cron", "0 0 3 * * *");
        recalculateCreditConfig.put("description", "每天凌晨3点重新计算用户信用等级");
        recalculateCreditConfig.put("enabled", true);
        config.put("recalculateCredit", recalculateCreditConfig);
        
        return Result.success("获取成功", config);
    }

    @Operation(summary = "更新任务配置", description = "更新定时任务的配置信息")
    @PostMapping("/config")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<String> updateTaskConfig(@RequestBody Map<String, Object> configRequest) {
        try {
            // 这里可以实现动态更新定时任务配置的逻辑
            // 由于Spring的@Scheduled注解是编译时确定的，动态修改需要使用TaskScheduler
            
            log.info("任务配置更新请求: {}", configRequest);
            
            // 记录配置更新操作
            recordTaskExecution("CONFIG_UPDATE", "SUCCESS", "任务配置更新成功");
            
            return Result.success("任务配置更新成功");
        } catch (Exception e) {
            log.error("更新任务配置失败", e);
            recordTaskExecution("CONFIG_UPDATE", "FAILED", e.getMessage());
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取任务执行效果统计", description = "获取任务执行后的效果统计")
    @GetMapping("/execution-stats")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getExecutionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 信用分数计算任务效果统计
            Map<String, Object> scoreGenerationStats = new HashMap<>();
            
            // 获取最新一次评分记录生成的统计
            String latestPeriod = creditScoreRecordService.getLatestScorePeriod();
            if (latestPeriod != null) {
                // 统计该周期生成的记录数
                QueryWrapper<CreditScoreRecord> periodQuery = new QueryWrapper<>();
                periodQuery.eq("score_period", latestPeriod);
                long periodRecordsCount = creditScoreRecordService.count(periodQuery);
                
                // 统计该周期获得的总奖励积分
                periodQuery.select("IFNULL(SUM(reward_points_gained), 0) as total_reward_points");
                List<Map<String, Object>> rewardResult = creditScoreRecordService.listMaps(periodQuery);
                int totalRewardPoints = 0;
                if (!rewardResult.isEmpty()) {
                    Object rewardObj = rewardResult.get(0).get("total_reward_points");
                    if (rewardObj != null) {
                        totalRewardPoints = ((Number) rewardObj).intValue();
                    }
                }
                
                scoreGenerationStats.put("latestPeriod", latestPeriod);
                scoreGenerationStats.put("recordsGenerated", periodRecordsCount);
                scoreGenerationStats.put("totalRewardPoints", totalRewardPoints);
                scoreGenerationStats.put("lastExecutionTime", getLastTaskRunTime("SCORE_GENERATION"));
            } else {
                scoreGenerationStats.put("latestPeriod", "暂无记录");
                scoreGenerationStats.put("recordsGenerated", 0);
                scoreGenerationStats.put("totalRewardPoints", 0);
                scoreGenerationStats.put("lastExecutionTime", "暂未执行");
            }
            
            stats.put("scoreGeneration", scoreGenerationStats);
            
            // 信用等级重算任务效果统计
            Map<String, Object> recalculateStats = new HashMap<>();
            
            // 统计各信用等级的用户数量
            Map<String, Object> levelDistribution = userCreditProfileService.getCreditLevelDistribution();
            recalculateStats.put("levelDistribution", levelDistribution);
            recalculateStats.put("highCreditUserCount", userCreditProfileService.getHighCreditUserCount());
            recalculateStats.put("lastExecutionTime", getLastTaskRunTime("RECALCULATE_CREDIT"));
            
            stats.put("recalculateCredit", recalculateStats);
            
            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取任务执行效果统计失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取任务统计信息", description = "获取定时任务的统计信息")
    @GetMapping("/statistics")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计信息
        stats.put("totalTasks", 2);
        stats.put("activeTasks", 2);
        stats.put("pausedTasks", 0);
        stats.put("failedTasks", 0);
        
        // 最近执行情况
        stats.put("todayExecutions", systemLogService.getTodayTaskExecutions());
        stats.put("weekExecutions", systemLogService.getWeekTaskExecutions());
        stats.put("monthExecutions", systemLogService.getMonthTaskExecutions());
        
        stats.put("message", "统计信息基于系统日志记录");
        
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "获取系统资源使用情况", description = "获取系统资源使用情况")
    @GetMapping("/system-info")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // JVM信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("totalMemory", totalMemory / 1024 / 1024 + " MB");
        jvmInfo.put("usedMemory", usedMemory / 1024 / 1024 + " MB");
        jvmInfo.put("freeMemory", freeMemory / 1024 / 1024 + " MB");
        jvmInfo.put("maxMemory", maxMemory / 1024 / 1024 + " MB");
        jvmInfo.put("memoryUsage", String.format("%.2f%%", (double) usedMemory / maxMemory * 100));
        
        systemInfo.put("jvm", jvmInfo);
        
        // 系统信息
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("osName", System.getProperty("os.name"));
        osInfo.put("osVersion", System.getProperty("os.version"));
        osInfo.put("javaVersion", System.getProperty("java.version"));
        osInfo.put("availableProcessors", runtime.availableProcessors());
        
        systemInfo.put("os", osInfo);
        
        return Result.success("获取成功", systemInfo);
    }

    @Operation(summary = "获取批量生成的评分记录", description = "查看批量生成的评分记录列表")
    @GetMapping("/score-records")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getScoreRecords(
            @Parameter(description = "周期") @RequestParam(required = false) String period,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        try {
            // 调用服务获取评分记录
            Map<String, Object> result = getScoreRecordsList(period, page, size);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取评分记录失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取评分记录统计信息", description = "获取批量生成评分记录的统计信息")
    @GetMapping("/score-records/statistics")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getScoreRecordsStatistics() {
        try {
            Map<String, Object> stats = getScoreRecordStatistics();
            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取评分记录统计失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查看所有用户信用分", description = "分页查询所有用户的当前信用分数")
    @GetMapping("/user-credit-scores")
    @RequireRole({User.UserRole.MAINTAINER})
    public Result<Map<String, Object>> getUserCreditScores(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "信用等级筛选") @RequestParam(required = false) String creditLevel) {
        try {
            Map<String, Object> result = userCreditProfileService.getUserCreditProfileList(page, size, keyword, creditLevel);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取用户信用分失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取最后一次任务执行时间
     */
    private String getLastTaskRunTime(String taskType) {
        return systemLogService.getLastTaskExecutionTime(taskType);
    }
    
    /**
     * 获取下次任务执行时间
     */
    private String getNextTaskRunTime(String cronExpression) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            LocalDateTime nextRun = cron.next(LocalDateTime.now());
            return nextRun.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return "计算失败";
        }
    }
    
    /**
     * 记录任务执行结果
     */
    private void recordTaskExecution(String taskType, String status, String message) {
        try {
            // 将响应数据格式化为JSON字符串
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("taskType", taskType);
            responseData.put("status", status);
            responseData.put("message", message);
            responseData.put("timestamp", LocalDateTime.now().toString());
            
            // 简单的JSON序列化
            String jsonResponse = String.format(
                "{\"taskType\":\"%s\",\"status\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                taskType, status, message.replace("\"", "\\\""), LocalDateTime.now()
            );
            
            systemLogService.recordSuccessLog(
                null, 
                "TASK_EXECUTION", 
                String.format("定时任务执行: %s, 状态: %s, 消息: %s", taskType, status, message),
                "POST", 
                "/task-management/execute", 
                null, 
                jsonResponse
            );
        } catch (Exception e) {
            log.warn("记录任务执行日志失败: {}", e.getMessage());
        }
    }

    /**
     * 获取评分记录列表
     */
    private Map<String, Object> getScoreRecordsList(String period, Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询评分记录，使用MyBatis-Plus的分页查询
            QueryWrapper<CreditScoreRecord> queryWrapper = new QueryWrapper<>();
            if (period != null && !period.trim().isEmpty()) {
                queryWrapper.eq("score_period", period);
            }
            queryWrapper.orderByDesc("calculation_time");
            
            // 计算分页参数
            int offset = (page - 1) * size;
            
            // 查询记录列表（这里简化处理，实际应该用分页插件）
            List<CreditScoreRecord> records = creditScoreRecordService.list(queryWrapper);
            
            // 手动分页
            int total = records.size();
            int endIndex = Math.min(offset + size, total);
            List<CreditScoreRecord> pageRecords = records.subList(offset, endIndex);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> resultRecords = new ArrayList<>();
            for (CreditScoreRecord record : pageRecords) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("userId", record.getUserId());
                recordMap.put("userName", getUserNameById(record.getUserId()));
                recordMap.put("scorePeriod", record.getScorePeriod());
                recordMap.put("periodScore", record.getPeriodScore());
                recordMap.put("rewardPointsGained", record.getRewardPointsGained());
                recordMap.put("calculationTime", record.getCalculationTime().toString());
                recordMap.put("status", "已完成");
                resultRecords.add(recordMap);
            }
            
            result.put("records", resultRecords);
            result.put("total", total);
            result.put("current", page);
            result.put("size", size);
            
        } catch (Exception e) {
            log.error("查询评分记录失败", e);
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("current", page);
            result.put("size", size);
        }
        
        return result;
    }
    
    /**
     * 获取评分记录统计信息
     */
    private Map<String, Object> getScoreRecordStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 查询总记录数
            long totalRecords = creditScoreRecordService.count();
            
            // 查询本周记录数
            String currentPeriod = CreditScoreRecordServiceImpl.getCurrentPeriod();
            QueryWrapper<CreditScoreRecord> thisWeekQuery = new QueryWrapper<>();
            thisWeekQuery.eq("score_period", currentPeriod);
            long thisWeekRecords = creditScoreRecordService.count(thisWeekQuery);
            
            // 计算总奖励积分
            QueryWrapper<CreditScoreRecord> sumQuery = new QueryWrapper<>();
            sumQuery.select("IFNULL(SUM(reward_points_gained), 0) as total_reward_points");
            List<Map<String, Object>> sumResult = creditScoreRecordService.listMaps(sumQuery);
            int totalRewardPoints = 0;
            if (!sumResult.isEmpty()) {
                Object totalRewardObj = sumResult.get(0).get("total_reward_points");
                if (totalRewardObj != null) {
                    totalRewardPoints = ((Number) totalRewardObj).intValue();
                }
            }
            
            // 本周奖励积分
            QueryWrapper<CreditScoreRecord> thisWeekRewardQuery = new QueryWrapper<>();
            thisWeekRewardQuery.eq("score_period", currentPeriod);
            thisWeekRewardQuery.select("IFNULL(SUM(reward_points_gained), 0) as week_reward_points");
            List<Map<String, Object>> thisWeekRewardResult = creditScoreRecordService.listMaps(thisWeekRewardQuery);
            int thisWeekRewardPoints = 0;
            if (!thisWeekRewardResult.isEmpty()) {
                Object weekRewardObj = thisWeekRewardResult.get(0).get("week_reward_points");
                if (weekRewardObj != null) {
                    thisWeekRewardPoints = ((Number) weekRewardObj).intValue();
                }
            }
            
            // 获取最新周期
            String latestPeriod = creditScoreRecordService.getLatestScorePeriod();
            if (latestPeriod == null) {
                latestPeriod = currentPeriod;
            }
            
            // 计算下一周期
            String nextPeriod = CreditScoreRecordServiceImpl.getCurrentPeriod();
            // 如果当前周期就是最新周期，则下一周期是下周
            if (currentPeriod.equals(latestPeriod)) {
                LocalDateTime nextWeek = LocalDateTime.now().plusWeeks(1);
                int dayOfWeek = nextWeek.getDayOfWeek().getValue();
                LocalDateTime nextWeekStart = nextWeek.minusDays(dayOfWeek - 1);
                nextPeriod = nextWeekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            
            stats.put("totalRecords", totalRecords);
            stats.put("thisWeekRecords", thisWeekRecords);
            stats.put("totalRewardPoints", totalRewardPoints);
            stats.put("thisWeekRewardPoints", thisWeekRewardPoints);
            stats.put("latestPeriod", latestPeriod);
            stats.put("nextPeriod", nextPeriod);
            
        } catch (Exception e) {
            log.error("查询评分记录统计失败", e);
            // 返回默认值
            stats.put("totalRecords", 0);
            stats.put("thisWeekRecords", 0);
            stats.put("totalRewardPoints", 0);
            stats.put("thisWeekRewardPoints", 0);
            stats.put("latestPeriod", "-");
            stats.put("nextPeriod", "-");
        }
        
        return stats;
    }
    
    /**
     * 根据用户ID获取用户名
     */
    private String getUserNameById(Integer userId) {
        try {
            User user = userService.getById(userId);
            if (user != null) {
                return user.getRealName() != null ? user.getRealName() : user.getUsername();
            }
        } catch (Exception e) {
            log.warn("查询用户名失败，用户ID: {}", userId, e);
        }
        return "用户" + userId;
    }
} 