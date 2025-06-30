package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.entity.SystemLog;

import java.util.Map;

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

    /**
     * 获取任务执行历史
     */
    Map<String, Object> getTaskExecutionHistory(String taskName, Integer page, Integer size);

    /**
     * 获取今日任务执行次数
     */
    Integer getTodayTaskExecutions();

    /**
     * 获取本周任务执行次数
     */
    Integer getWeekTaskExecutions();

    /**
     * 获取本月任务执行次数
     */
    Integer getMonthTaskExecutions();

    /**
     * 获取最后一次任务执行时间
     */
    String getLastTaskExecutionTime(String taskType);
} 