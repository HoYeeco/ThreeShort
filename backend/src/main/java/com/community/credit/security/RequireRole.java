
package com.community.credit.security;

import com.community.credit.entity.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于标记需要特定角色才能访问的接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 需要的角色
     */
    User.UserRole[] value();
    
    /**
     * 是否允许用户访问自己的资源
     * 当设置为true时，用户可以访问自己的资源，即使角色不匹配
     */
    boolean allowSelf() default false;

} 