package com.community.credit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.UserQueryRequest;
import com.community.credit.dto.UserUpdateRequest;
import com.community.credit.entity.User;

/**
 * 用户服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     */
    IPage<User> queryUsers(UserQueryRequest request);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Integer userId, UserUpdateRequest request);

    /**
     * 获取用户详细信息
     */
    User getUserDetail(Integer userId);

    /**
     * 检查用户权限
     */
    boolean checkUserPermission(Integer currentUserId, Integer targetUserId);

    /**
     * 根据用户名获取用户
     */
    User getByUsername(String username);
} 