package com.community.credit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.community.credit.dto.ExchangeRecordQueryRequest;
import com.community.credit.entity.PointExchangeRecord;

import java.util.List;
import java.util.Map;

/**
 * 积分兑换记录服务接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
public interface PointExchangeRecordService extends IService<PointExchangeRecord> {

    /**
     * 分页查询兑换记录
     * 
     * @param request 查询条件
     * @return 兑换记录分页列表
     */
    IPage<PointExchangeRecord> getExchangeRecordList(ExchangeRecordQueryRequest request);

    /**
     * 获取用户兑换记录
     * 
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 兑换记录列表
     */
    List<PointExchangeRecord> getUserExchangeRecords(Integer userId, Integer limit);

    /**
     * 获取兑换统计数据
     * 
     * @return 统计数据
     */
    Map<String, Object> getExchangeStatistics();

    /**
     * 获取热门商品统计
     * 
     * @param limit 限制数量
     * @return 热门商品列表
     */
    List<Map<String, Object>> getPopularProducts(Integer limit);
} 