package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 获取用户在指定周期的上报统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_reports, " +
            "COUNT(CASE WHEN status = 'APPROVED' THEN 1 END) as approved_count, " +
            "COUNT(CASE WHEN status = 'REJECTED' THEN 1 END) as rejected_count, " +
            "COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count, " +
            "SUM(CASE WHEN status = 'APPROVED' THEN score_awarded ELSE 0 END) as total_score " +
            "FROM credit_reports " +
            "WHERE user_id = #{userId} " +
            "AND report_time >= #{periodStart} " +
            "AND report_time < #{periodEnd}")
    Map<String, Object> getUserReportStatsByPeriod(@Param("userId") Integer userId, 
                                                   @Param("periodStart") String periodStart, 
                                                   @Param("periodEnd") String periodEnd);

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

    /**
     * 分页查询信用报告列表（关联查询行为类型名称）
     * 
     * @param page 分页参数
     * @param userId 用户ID（可选）
     * @param behaviorTypeId 行为类型ID（可选）
     * @param status 状态（可选）
     * @param reviewerId 审核员ID（可选）
     * @param keyword 关键词（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT cr.*, cbt.name as behavior_type " +
            "FROM credit_reports cr " +
            "LEFT JOIN credit_behavior_types cbt ON cr.behavior_type_id = cbt.id " +
            "WHERE 1=1 " +
            "<if test='userId != null'> AND cr.user_id = #{userId} </if>" +
            "<if test='behaviorTypeId != null'> AND cr.behavior_type_id = #{behaviorTypeId} </if>" +
            "<if test='status != null and status != &quot;&quot;'> AND cr.status = #{status} </if>" +
            "<if test='reviewerId != null'> AND cr.reviewer_id = #{reviewerId} </if>" +
            "<if test='keyword != null and keyword != &quot;&quot;'> AND (cr.title LIKE CONCAT('%', #{keyword}, '%') OR cr.description LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "<if test='startTime != null and startTime != &quot;&quot;'> AND cr.report_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null and endTime != &quot;&quot;'> AND cr.report_time &lt;= #{endTime} </if>" +
            "ORDER BY cr.created_time DESC" +
            "</script>")
    IPage<Map<String, Object>> selectReportsWithBehaviorType(Page<Map<String, Object>> page,
                                                             @Param("userId") Integer userId,
                                                             @Param("behaviorTypeId") Integer behaviorTypeId,
                                                             @Param("status") String status,
                                                             @Param("reviewerId") Integer reviewerId,
                                                             @Param("keyword") String keyword,
                                                             @Param("startTime") String startTime,
                                                             @Param("endTime") String endTime);
}