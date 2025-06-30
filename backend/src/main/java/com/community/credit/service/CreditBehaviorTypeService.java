package com.community.credit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.entity.CreditBehaviorType;

import java.util.List;

/**
 * 信用行为类型服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface CreditBehaviorTypeService extends IService<CreditBehaviorType> {

    /**
     * 获取所有启用的行为类型
     * 
     * @return 行为类型列表
     */
    List<CreditBehaviorType> getActiveBehaviorTypes();

    /**
     * 根据分类获取行为类型
     * 
     * @param category 行为分类
     * @return 行为类型列表
     */
    List<CreditBehaviorType> getBehaviorTypesByCategory(CreditBehaviorType.BehaviorCategory category);
} 