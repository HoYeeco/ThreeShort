package com.community.credit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.community.credit.common.Result;
import com.community.credit.dto.ExchangeRecordQueryRequest;
import com.community.credit.entity.PointExchangeRecord;
import com.community.credit.entity.User;
import com.community.credit.security.RequireRole;
import com.community.credit.service.PointExchangeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换记录控制器
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Tag(name = "积分兑换记录", description = "兑换记录查询和统计接口")
@RestController
@RequestMapping("/exchange-record")
public class PointExchangeRecordController {

    @Autowired
    private PointExchangeRecordService pointExchangeRecordService;

    @Operation(summary = "获取兑换记录列表", description = "分页查询兑换记录，支持条件筛选")
    @GetMapping("/list")
    @RequireRole({User.UserRole.ADMIN})
    public Result<IPage<PointExchangeRecord>> getExchangeRecordList(ExchangeRecordQueryRequest request) {
        IPage<PointExchangeRecord> page = pointExchangeRecordService.getExchangeRecordList(request);
        return Result.success("获取成功", page);
    }

    @Operation(summary = "获取用户兑换记录", description = "获取指定用户的兑换记录")
    @GetMapping("/user/{userId}")
    @RequireRole(value = {User.UserRole.RESIDENT, User.UserRole.ADMIN}, allowSelf = true)
    public Result<List<PointExchangeRecord>> getUserExchangeRecords(@PathVariable Integer userId,
                                                                   @RequestParam(defaultValue = "10") Integer limit) {
        List<PointExchangeRecord> records = pointExchangeRecordService.getUserExchangeRecords(userId, limit);
        return Result.success("获取成功", records);
    }

    @Operation(summary = "获取当前用户兑换记录", description = "获取当前登录用户的兑换记录")
    @GetMapping("/current-user")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<List<PointExchangeRecord>> getCurrentUserExchangeRecords(@RequestParam Integer userId,
                                                                          @RequestParam(defaultValue = "10") Integer limit) {
        List<PointExchangeRecord> records = pointExchangeRecordService.getUserExchangeRecords(userId, limit);
        return Result.success("获取成功", records);
    }

    @Operation(summary = "获取兑换记录详情", description = "根据ID获取兑换记录详情")
    @GetMapping("/{id}")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<PointExchangeRecord> getExchangeRecordById(@PathVariable Integer id) {
        PointExchangeRecord record = pointExchangeRecordService.getById(id);
        if (record == null) {
            return Result.error("兑换记录不存在");
        }
        return Result.success("获取成功", record);
    }

    @Operation(summary = "获取兑换统计", description = "获取系统兑换统计数据")
    @GetMapping("/statistics")
    @RequireRole({User.UserRole.ADMIN})
    public Result<Map<String, Object>> getExchangeStatistics() {
        Map<String, Object> stats = pointExchangeRecordService.getExchangeStatistics();
        return Result.success("获取成功", stats);
    }

    @Operation(summary = "获取热门商品", description = "获取兑换量最多的热门商品")
    @GetMapping("/popular-products")
    @RequireRole({User.UserRole.RESIDENT, User.UserRole.ADMIN})
    public Result<List<Map<String, Object>>> getPopularProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Map<String, Object>> products = pointExchangeRecordService.getPopularProducts(limit);
        return Result.success("获取成功", products);
    }

    @Operation(summary = "删除兑换记录", description = "删除指定的兑换记录")
    @DeleteMapping("/{recordId}")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> deleteRecord(@PathVariable Integer recordId) {
        pointExchangeRecordService.deleteRecord(recordId);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除兑换记录", description = "批量删除多个兑换记录（仅管理员）")
    @DeleteMapping("/batch")
    @RequireRole({User.UserRole.ADMIN})
    public Result<String> batchDeleteExchangeRecords(@RequestBody List<Integer> ids) {
        boolean removed = pointExchangeRecordService.removeByIds(ids);
        if (removed) {
            return Result.success("批量删除成功");
        } else {
            return Result.error("批量删除失败");
        }
    }

    @Operation(summary = "导出兑换记录", description = "导出兑换记录为CSV文件")
    @GetMapping("/export")
    @RequireRole({User.UserRole.ADMIN})
    public void exportRecords(
            @Parameter(description = "用户ID") @RequestParam(required = false) Integer userId,
            @Parameter(description = "商品ID") @RequestParam(required = false) Integer productId,
            @Parameter(description = "状态") @RequestParam(required = false) PointExchangeRecord.ExchangeStatus status,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            HttpServletResponse response) throws IOException {
        
        // 构建查询条件
        ExchangeRecordQueryRequest queryRequest = new ExchangeRecordQueryRequest();
        queryRequest.setUserId(userId);
        queryRequest.setProductId(productId);
        queryRequest.setStatus(status);
        
        // 解析时间参数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (startTime != null && !startTime.isEmpty()) {
            queryRequest.setStartTime(LocalDateTime.parse(startTime, formatter));
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryRequest.setEndTime(LocalDateTime.parse(endTime, formatter));
        }
        
        queryRequest.setCurrent(1);
        queryRequest.setSize(Integer.MAX_VALUE); // 导出所有数据
        
        pointExchangeRecordService.exportRecords(queryRequest, response);
    }
} 