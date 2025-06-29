package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 获取商品分类统计
     * 
     * @return 分类统计数据
     */
    @Select("SELECT category, COUNT(*) as count FROM products " +
            "WHERE category IS NOT NULL GROUP BY category")
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 获取积分区间统计
     * 
     * @return 积分区间统计数据
     */
    @Select("SELECT " +
            "CASE " +
            "    WHEN points_required < 50 THEN '0-49' " +
            "    WHEN points_required < 100 THEN '50-99' " +
            "    WHEN points_required < 200 THEN '100-199' " +
            "    WHEN points_required < 500 THEN '200-499' " +
            "    ELSE '500+' " +
            "END as pointRange, " +
            "COUNT(*) as count " +
            "FROM products " +
            "GROUP BY " +
            "CASE " +
            "    WHEN points_required < 50 THEN '0-49' " +
            "    WHEN points_required < 100 THEN '50-99' " +
            "    WHEN points_required < 200 THEN '100-199' " +
            "    WHEN points_required < 500 THEN '200-499' " +
            "    ELSE '500+' " +
            "END " +
            "ORDER BY MIN(points_required)")
    List<Map<String, Object>> getPointsRangeStatistics();
} 