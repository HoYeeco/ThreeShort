package com.community.credit.dto;

import com.community.credit.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户查询请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "UserQueryRequest", description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "关键词搜索（用户名/真实姓名）", example = "张三")
    private String keyword;

    @Schema(description = "用户角色", example = "RESIDENT")
    private User.UserRole role;

    @Schema(description = "用户状态", example = "1")
    private Integer status;

    @Schema(description = "社区ID")
    private Integer communityId;
} 