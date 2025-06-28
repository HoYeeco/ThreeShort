package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.CreditReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 信用行为上报Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface CreditReportMapper extends BaseMapper<CreditReport> {

    /**
     * 获取用户上报统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_reports, " +
            "COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_count, " +
            "COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_count, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count, " +
            "SUM(CASE WHEN status = 'APPROVED' THEN score_awarded ELSE 0 END) as total_score " +
            "FROM credit_reports WHERE user_id = #{userId}")
    Map<String, Object> getUserReportStats(@Param("userId") Integer userId);

    /**
     * 获取用户最近的上报记录
     */
    @Select("SELECT * FROM credit_reports WHERE user_id = #{userId} " +
            "ORDER BY report_time DESC LIMIT #{limit}")
    List<CreditReport> getRecentReports(@Param("userId") Integer userId, @Param("limit") Integer limit);

    /**
     * 获取待审核上报数量（按审核员分组）
     * 
     * @return 统计结果
     */
    @Select("SELECT reviewer_id, COUNT(*) as count FROM credit_reports " +
            "WHERE status = 'PENDING' GROUP BY reviewer_id")
    List<Map<String, Object>> getPendingCountByReviewer();
}