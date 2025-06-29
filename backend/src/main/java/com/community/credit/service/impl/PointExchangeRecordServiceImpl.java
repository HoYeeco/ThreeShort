package com.community.credit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.credit.dto.ExchangeRecordQueryRequest;
import com.community.credit.entity.PointExchangeRecord;
import com.community.credit.mapper.PointExchangeRecordMapper;
import com.community.credit.service.PointExchangeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换记录服务实现类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Slf4j
@Service
public class PointExchangeRecordServiceImpl extends ServiceImpl<PointExchangeRecordMapper, PointExchangeRecord> implements PointExchangeRecordService {

    @Override
    public IPage<PointExchangeRecord> getExchangeRecordList(ExchangeRecordQueryRequest request) {
        Page<PointExchangeRecord> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<PointExchangeRecord> queryWrapper = new QueryWrapper<>();

        // 条件筛选
        if (request.getUserId() != null) {
            queryWrapper.eq("user_id", request.getUserId());
        }
        
        if (request.getProductId() != null) {
            queryWrapper.eq("product_id", request.getProductId());
        }
        
        if (StringUtils.hasText(request.getProductName())) {
            queryWrapper.like("product_name", request.getProductName());
        }
        
        if (request.getStatus() != null) {
            queryWrapper.eq("status", request.getStatus().getCode());
        }
        
        if (request.getStartTime() != null) {
            queryWrapper.ge("exchange_time", request.getStartTime());
        }
        
        if (request.getEndTime() != null) {
            queryWrapper.le("exchange_time", request.getEndTime());
        }
        
        if (request.getMinPoints() != null) {
            queryWrapper.ge("points_used", request.getMinPoints());
        }
        
        if (request.getMaxPoints() != null) {
            queryWrapper.le("points_used", request.getMaxPoints());
        }

        // 排序
        if ("points_used".equals(request.getSortField())) {
            if ("asc".equals(request.getSortOrder())) {
                queryWrapper.orderByAsc("points_used");
            } else {
                queryWrapper.orderByDesc("points_used");
            }
        } else if ("quantity".equals(request.getSortField())) {
            if ("asc".equals(request.getSortOrder())) {
                queryWrapper.orderByAsc("quantity");
            } else {
                queryWrapper.orderByDesc("quantity");
            }
        } else {
            // 默认按兑换时间排序
            queryWrapper.orderByDesc("exchange_time");
        }

        return this.page(page, queryWrapper);
    }

    @Override
    public List<PointExchangeRecord> getUserExchangeRecords(Integer userId, Integer limit) {
        LambdaQueryWrapper<PointExchangeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PointExchangeRecord::getUserId, userId)
                .orderByDesc(PointExchangeRecord::getExchangeTime);
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        return list(queryWrapper);
    }

    @Override
    public Map<String, Object> getExchangeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总兑换记录数
        long totalExchanges = this.count();
        stats.put("totalExchanges", totalExchanges);
        
        // 总兑换积分
        Long totalPoints = baseMapper.getTotalPointsUsed();
        stats.put("totalPointsUsed", totalPoints != null ? totalPoints : 0L);
        
        // 成功兑换数
        long successExchanges = this.count(new QueryWrapper<PointExchangeRecord>()
                .eq("status", PointExchangeRecord.ExchangeStatus.SUCCESS.getCode()));
        stats.put("successExchanges", successExchanges);
        
        // 今日兑换数
        long todayExchanges = baseMapper.getTodayExchangeCount();
        stats.put("todayExchanges", todayExchanges);
        
        // 本月兑换数
        long thisMonthExchanges = baseMapper.getThisMonthExchangeCount();
        stats.put("thisMonthExchanges", thisMonthExchanges);
        
        // 兑换状态分布
        List<Map<String, Object>> statusDistribution = baseMapper.getStatusDistribution();
        stats.put("statusDistribution", statusDistribution);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getPopularProducts(Integer limit) {
        return baseMapper.getPopularProducts(limit);
    }
} 