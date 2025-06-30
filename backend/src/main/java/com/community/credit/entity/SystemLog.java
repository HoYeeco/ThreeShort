package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_logs")
@Schema(name = "SystemLog", description = "系统操作日志")
public class SystemLog {

    @Schema(description = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "操作用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "操作类型")
    @TableField("operation_type")
    private String operationType;

    @Schema(description = "操作描述")
    @TableField("operation_desc")
    private String operationDesc;

    @Schema(description = "请求方法")
    @TableField("request_method")
    private String requestMethod;

    @Schema(description = "请求URL")
    @TableField("request_url")
    private String requestUrl;

    @Schema(description = "请求参数")
    @TableField("request_params")
    private String requestParams;

    @Schema(description = "响应数据")
    @TableField("response_data")
    private String responseData;

    @Schema(description = "IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @Schema(description = "用户代理")
    @TableField("user_agent")
    private String userAgent;

    @Schema(description = "执行时间（毫秒）")
    @TableField("execution_time")
    private Integer executionTime;

    @Schema(description = "执行状态")
    @TableField("status")
    private LogStatus status;

    @Schema(description = "错误信息")
    @TableField("error_message")
    private String errorMessage;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 日志状态枚举
     */
    public enum LogStatus {
        SUCCESS, FAILED, ERROR
    }
} 