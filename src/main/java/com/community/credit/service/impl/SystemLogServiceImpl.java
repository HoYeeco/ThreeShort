package com.community.credit.service.impl;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.entity.SystemLog;
import com.community.credit.mapper.SystemLogMapper;
import com.community.credit.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统日志服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Override
    @Async
    public void recordLog(Integer userId, String operationType, String operationDesc,
                         String requestMethod, String requestUrl, String requestParams, String responseData) {
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUserId(userId);
            systemLog.setOperationType(operationType);
            systemLog.setOperationDesc(operationDesc);
            systemLog.setRequestMethod(requestMethod);
            systemLog.setRequestUrl(requestUrl);
            systemLog.setRequestParams(requestParams);
            systemLog.setResponseData(responseData);
            systemLog.setStatus(SystemLog.LogStatus.SUCCESS);

            // 获取请求信息
            setRequestInfo(systemLog);

            this.save(systemLog);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void recordSuccessLog(Integer userId, String operationType, String operationDesc,
                                String requestMethod, String requestUrl, String requestParams, String responseData) {
        recordLog(userId, operationType, operationDesc, requestMethod, requestUrl, requestParams, responseData);
    }

    @Override
    @Async
    public void recordFailureLog(Integer userId, String operationType, String operationDesc,
                                 String requestMethod, String requestUrl, String requestParams, String errorMessage) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setRequestParams(requestParams);
        log.setResponseData(errorMessage);
        log.setStatus(SystemLog.LogStatus.FAILED);
        log.setErrorMessage(errorMessage);
        log.setCreatedTime(LocalDateTime.now());

        save(log);
    }

    /**
     * 设置请求相关信息
     */
    private void setRequestInfo(SystemLog systemLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 获取IP地址
                String ipAddress = getClientIP(request);
                systemLog.setIpAddress(ipAddress);

                // 获取User-Agent
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
                systemLog.setUserAgent(userAgent);
            }
        } catch (Exception e) {
            log.warn("获取请求信息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个非unknown的有效IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    @Override
    public Map<String, Object> getTaskExecutionHistory(String taskName, Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "TASK_EXECUTION");
        
        if (taskName != null && !taskName.isEmpty()) {
            queryWrapper.like("operation_desc", taskName);
        }
        
        queryWrapper.orderByDesc("created_time");
        
        // 分页查询
        Page<SystemLog> pageRequest = new Page<>(page, size);
        IPage<SystemLog> pageResult = this.page(pageRequest, queryWrapper);
        
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("current", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        
        return result;
    }

    @Override
    public Integer getTodayTaskExecutions() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "TASK_EXECUTION");
        queryWrapper.ge("created_time", todayStart);
        
        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public Integer getWeekTaskExecutions() {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "TASK_EXECUTION");
        queryWrapper.ge("created_time", weekStart);
        
        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public Integer getMonthTaskExecutions() {
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "TASK_EXECUTION");
        queryWrapper.ge("created_time", monthStart);
        
        return Math.toIntExact(this.count(queryWrapper));
    }

    @Override
    public String getLastTaskExecutionTime(String taskType) {
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "TASK_EXECUTION");
        queryWrapper.like("operation_desc", taskType);
        queryWrapper.orderByDesc("created_time");
        queryWrapper.last("LIMIT 1");
        
        SystemLog lastLog = this.getOne(queryWrapper);
        if (lastLog != null && lastLog.getCreatedTime() != null) {
            return lastLog.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        return "暂无执行记录";
    }
} 