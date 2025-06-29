package com.community.credit.config;

import com.community.credit.security.RoleInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-path:/uploads/}")
    private String uploadPath;

    @Autowired
    private RoleInterceptor roleInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态文件访问映射
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:" + uploadPath);
        
        // 添加其他静态资源映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/**",  // 排除认证相关接口
                    "/api/files/**", // 排除文件访问接口
                    "/api/agreements/**", // 排除公约相关接口（暂时允许无需登录访问）
                    "/api/doc.html", // 排除Swagger文档
                    "/api/swagger-ui/**", // 排除Swagger UI
                    "/api/v3/api-docs/**" // 排除API文档
                );
    }