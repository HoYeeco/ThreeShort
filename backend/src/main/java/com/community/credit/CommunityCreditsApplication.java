package com.community.credit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 社区居民诚信管理系统启动类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CommunityCreditsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityCreditsApplication.class, args);
        System.out.println("===================================");
        System.out.println("社区居民诚信管理系统启动成功！");
        System.out.println("接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("数据库监控: http://localhost:8080/api/druid/index.html");
        System.out.println("===================================");
    }
} 