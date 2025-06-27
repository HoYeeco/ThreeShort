package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.PointExchangeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 积分兑换记录Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface PointExchangeRecordMapper extends BaseMapper<PointExchangeRecord> {

    /**
     * 获取总兑换积分
     * 
     * @return 总积分
     */
    @Select("SELECT SUM(points_used) FROM point_exchange_records WHERE status = 'SUCCESS'")
    Long getTotalPointsUsed();

    /**
     * 获取今日兑换数
     * 
     * @return 今日兑换数
     */
    @Select("SELECT COUNT(*) FROM point_exchange_records " +
            "WHERE DATE(exchange_time) = CURDATE() AND status = 'SUCCESS'")
    Long getTodayExchangeCount();

    /**
     * 获取本月兑换数
     * 
     * @return 本月兑换数
     */
    @Select("SELECT COUNT(*) FROM point_exchange_records " +
            "WHERE YEAR(exchange_time) = YEAR(CURDATE()) " +
            "AND MONTH(exchange_time) = MONTH(CURDATE()) " +
            "AND status = 'SUCCESS'")
    Long getThisMonthExchangeCount();

    /**
     * 获取兑换状态分布
     * 
     * @return 状态分布数据
     */
    @Select("SELECT status, COUNT(*) as count FROM point_exchange_records GROUP BY status")
    List<Map<String, Object>> getStatusDistribution();

    /**
     * 获取热门商品
     * 
     * @param limit 限制数量
     * @return 热门商品列表
     */
    @Select("SELECT " +
            "product_id, " +
            "product_name, " +
            "SUM(quantity) as totalQuantity, " +
            "COUNT(*) as exchangeCount, " +
            "SUM(points_used) as totalPoints " +
            "FROM point_exchange_records " +
            "WHERE status = 'SUCCESS' " +
            "GROUP BY product_id, product_name " +
            "ORDER BY totalQuantity DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getPopularProducts(@Param("limit") Integer limit);

    /**
     * 获取用户兑换统计
     * 
     * @param userId 用户ID
     * @return 兑换统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_exchanges, " +
            "SUM(points_used) as total_points_used, " +
            "SUM(quantity) as total_quantity " +
            "FROM point_exchange_records " +
            "WHERE user_id = #{userId} AND status = 'SUCCESS'")
    Map<String, Object> getUserExchangeStats(@Param("userId") Integer userId);
} 