package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.UserQueryRequest;
import com.community.credit.dto.UserUpdateRequest;
import com.community.credit.entity.User;
import com.community.credit.mapper.UserMapper;
import com.community.credit.service.SystemLogService;
import com.community.credit.service.UserCreditProfileService;
import com.community.credit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private UserCreditProfileService userCreditProfileService;

    @Override
    public IPage<User> queryUsers(UserQueryRequest request) {
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> 
                wrapper.like("username", request.getKeyword())
                       .or()
                       .like("real_name", request.getKeyword())
            );
        }

        // 角色过滤
        if (request.getRole() != null) {
            queryWrapper.eq("role", request.getRole());
        }

        // 状态过滤
        if (request.getStatus() != null) {
            queryWrapper.eq("status", request.getStatus());
        }

        // 社区过滤
        if (request.getCommunityId() != null) {
            queryWrapper.eq("community_id", request.getCommunityId());
        }

        // 排序
        queryWrapper.orderByDesc("created_time");

        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public void updateUserInfo(Integer userId, UserUpdateRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查手机号是否已被其他用户使用
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            User existingPhoneUser = lambdaQuery()
                    .eq(User::getPhone, request.getPhone())
                    .ne(User::getId, userId)
                    .one();
            if (existingPhoneUser != null) {
                throw new RuntimeException("手机号已被其他用户使用");
            }
        }

        // 检查身份证号是否已被其他用户使用
        if (request.getIdCard() != null && !request.getIdCard().isEmpty()) {
            User existingIdCardUser = lambdaQuery()
                    .eq(User::getIdCard, request.getIdCard())
                    .ne(User::getId, userId)
                    .one();
            if (existingIdCardUser != null) {
                throw new RuntimeException("身份证号已被其他用户使用");
            }
        }

        // 更新用户信息
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setIdCard(request.getIdCard());
        user.setAddress(request.getAddress());
        user.setAvatar(request.getAvatar());

        updateById(user);

        log.info("用户信息更新成功，用户ID: {}", userId);
    }

    @Override
    public User getUserDetail(Integer userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 脱敏处理
        if (user.getPhone() != null && user.getPhone().length() > 7) {
            user.setPhone(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(7));
        }
        if (user.getIdCard() != null && user.getIdCard().length() > 10) {
            user.setIdCard(user.getIdCard().substring(0, 6) + "********" + user.getIdCard().substring(14));
        }

        return user;
    }

    @Override
    public boolean checkUserPermission(Integer currentUserId, Integer targetUserId) {
        // 简化版权限检查：用户只能操作自己的信息，或者管理员可以操作所有用户
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        // 检查当前用户是否为管理员
        User currentUser = getById(currentUserId);
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    @Override
    public User getByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional
    public void resetPassword(Integer userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 简化版密码加密，实际应该使用BCrypt等安全算法
        user.setPassword(newPassword);
        updateById(user);

        log.info("用户密码重置成功，用户ID: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }

        // 检查新密码是否与原密码相同
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 更新密码
        user.setPassword(newPassword);
        updateById(user);

        log.info("用户密码修改成功，用户ID: {}", userId);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = this.count();
        stats.put("total", totalUsers);
        
        // 活跃用户数（状态为1的用户）
        QueryWrapper<User> activeQuery = new QueryWrapper<>();
        activeQuery.eq("status", 1);
        long activeUsers = this.count(activeQuery);
        stats.put("active", activeUsers);
        
        // 今日新增用户数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        QueryWrapper<User> todayQuery = new QueryWrapper<>();
        todayQuery.ge("created_time", todayStart);
        long todayUsers = this.count(todayQuery);
        stats.put("today", todayUsers);
        
        // 高信用等级用户数（AA和AAA等级）
        // 这里需要关联user_credit_profiles表
        stats.put("creditHigh", userCreditProfileService.getHighCreditUserCount());
        
        return stats;
    }
} 