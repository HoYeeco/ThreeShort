package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "UserUpdateRequest", description = "用户信息更新请求")
public class UserUpdateRequest {

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "手机号", example = "13888888888")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "身份证号", example = "420103199510274938")
    @NotBlank(message = "身份证号不能为空")
    @com.community.credit.validation.IdCard(allowEmpty = false, message = "身份证号格式不正确")
    private String idCard;

    @Schema(description = "住址", example = "北京市朝阳区某小区")
    @Size(max = 200, message = "住址长度不能超过200个字符")
    private String address;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
} 