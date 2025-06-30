package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.entity.SystemLog;

/**
 * 系统日志服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface SystemLogService extends IService<SystemLog> {

    /**
     * 记录操作日志
     */
    void recordLog(Integer userId, String operationType, String operationDesc,
                   String requestMethod, String requestUrl, String requestParams, String responseData);

    /**
     * 记录成功日志
     */
    void recordSuccessLog(Integer userId, String operationType, String operationDesc,
                          String requestMethod, String requestUrl, String requestParams, String responseData);

    /**
     * 记录失败日志
     */
    void recordFailureLog(Integer userId, String operationType, String operationDesc,
                          String requestMethod, String requestUrl, String requestParams, String errorMessage);
} 