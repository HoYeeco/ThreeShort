package com.community.credit.service.impl;

import cn.hutool.http.HttpUtil;
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
        try {
            SystemLog systemLog = new SystemLog();
            systemLog.setUserId(userId);
            systemLog.setOperationType(operationType);
            systemLog.setOperationDesc(operationDesc);
            systemLog.setRequestMethod(requestMethod);
            systemLog.setRequestUrl(requestUrl);
            systemLog.setRequestParams(requestParams);
            systemLog.setErrorMessage(errorMessage);
            systemLog.setStatus(SystemLog.LogStatus.FAILED);
            
            save(systemLog);
        } catch (Exception e) {
            log.error("记录失败日志时发生异常", e);
        }
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
} 