package com.community.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@Schema(name = "RegisterRequest", description = "注册请求")
public class RegisterRequest {

    @Schema(description = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Schema(description = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Schema(description = "确认密码", required = true, example = "123456")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "真实姓名", required = true, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "手机号", required = true, example = "13888888888")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "身份证号", example = "420103199510274939")
    @com.community.credit.validation.IdCard(allowEmpty = true, message = "身份证号格式不正确")
    private String idCard;

    @Schema(description = "住址", example = "北京市朝阳区某小区")
    @Size(max = 200, message = "住址长度不能超过200个字符")
    private String address;

    @Schema(description = "所属社区ID", example = "1")
    private Integer communityId;
} 