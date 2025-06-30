package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 信用行为上报请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "CreditReportRequest", description = "信用行为上报请求")
public class CreditReportRequest {

    @Schema(description = "上报用户ID", example = "1")
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @Schema(description = "行为类型ID", example = "1")
    @NotNull(message = "行为类型ID不能为空")
    private Integer behaviorTypeId;

    @Schema(description = "上报标题", required = true, example = "小区志愿服务活动")
    @NotBlank(message = "上报标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Schema(description = "行为描述", required = true, example = "参与小区清洁志愿服务，持续2小时")
    @NotBlank(message = "行为描述不能为空")
    @Size(max = 1000, message = "描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "证据文件列表", example = "[\"file1.jpg\", \"file2.mp4\"]")
    @Size(max = 10, message = "证据文件不能超过10个")
    private List<String> evidenceFiles;

    @Schema(description = "发生地点", example = "小区花园")
    @Size(max = 200, message = "地点长度不能超过200个字符")
    private String location;

    @Schema(description = "涉及人员姓名（用于显示）", example = "张三")
    @Size(max = 500, message = "涉及人员长度不能超过500个字符")
    private String involvedPersons;

    @Schema(description = "涉及人员ID（用于信用分分配，如果是上报他人行为则必填）", example = "2")
    private Integer reportedUserId;
} 