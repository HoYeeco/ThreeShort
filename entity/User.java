package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
@Schema(name = "User", description = "用户信息")
public class User {

    @Schema(description = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    @JsonIgnore
    private String password;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "身份证号")
    @TableField("id_card")
    private String idCard;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "住址")
    @TableField("address")
    private String address;

    @Schema(description = "角色：RESIDENT居民/ADMIN管理员/MAINTAINER维护员")
    @TableField("role")
    private UserRole role;

    @Schema(description = "状态：1正常 0禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "所属社区ID")
    @TableField("community_id")
    private Integer communityId;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        RESIDENT, ADMIN, MAINTAINER
    }
} 