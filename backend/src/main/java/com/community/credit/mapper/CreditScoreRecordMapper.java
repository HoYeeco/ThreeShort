package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.CreditScoreRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 信用评分记录Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface CreditScoreRecordMapper extends BaseMapper<CreditScoreRecord> {

    /**
     * 获取用户历史评分记录
     */
    @Select("SELECT * FROM credit_score_records WHERE user_id = #{userId} ORDER BY calculation_time DESC")
    List<CreditScoreRecord> getUserScoreHistory(@Param("userId") Integer userId);

    /**
     * 根据用户ID和周期获取评分记录
     */
    @Select("SELECT * FROM credit_score_records WHERE user_id = #{userId} AND score_period = #{period}")
    CreditScoreRecord getScoreByPeriod(@Param("userId") Integer userId, @Param("period") String period);

    /**
     * 获取用户评分趋势数据
     */
    @Select("SELECT " +
            "csr.score_period as period, " +
            "csr.period_score as score, " +
            "csr.reward_points_gained as points, " +
            "csr.calculation_time as time " +
            "FROM credit_score_records csr " +
            "WHERE csr.user_id = #{userId} " +
            "ORDER BY csr.score_period ASC " +
            "LIMIT 12")
    List<Map<String, Object>> getUserScoreTrend(@Param("userId") Integer userId);

    /**
     * 获取所有用户的最新评分周期
     * 
     * @return 最新周期列表
     */
    @Select("SELECT DISTINCT score_period FROM credit_score_records " +
            "ORDER BY score_period DESC LIMIT 1")
    String getLatestScorePeriod();

    /**
     * 获取所有活跃用户ID
     * 
     * @return 用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM users WHERE status = 1")
    List<Integer> getAllActiveUserIds();
} 