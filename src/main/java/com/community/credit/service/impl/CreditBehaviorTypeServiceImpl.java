package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.entity.CreditBehaviorType;
import com.community.credit.mapper.CreditBehaviorTypeMapper;
import com.community.credit.service.CreditBehaviorTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 信用行为类型服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Service
public class CreditBehaviorTypeServiceImpl extends ServiceImpl<CreditBehaviorTypeMapper, CreditBehaviorType> implements CreditBehaviorTypeService {

    @Override
    public List<CreditBehaviorType> getActiveBehaviorTypes() {
        QueryWrapper<CreditBehaviorType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true)
                    .orderByAsc("category")
                    .orderByDesc("score_rule");
        return this.list(queryWrapper);
    }

    @Override
    public List<CreditBehaviorType> getBehaviorTypesByCategory(CreditBehaviorType.BehaviorCategory category) {
        QueryWrapper<CreditBehaviorType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true)
                    .eq("category", category)
                    .orderByDesc("score_rule");
        return this.list(queryWrapper);
    }
} 