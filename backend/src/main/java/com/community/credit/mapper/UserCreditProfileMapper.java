package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.UserCreditProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户信用档案Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface UserCreditProfileMapper extends BaseMapper<UserCreditProfile> {
    
    /**
     * 获取信用排行榜（包含用户姓名）
     * 
     * @param limit 限制数量
     * @return 排行榜列表
     */
    @Select("SELECT ucp.*, u.real_name " +
            "FROM user_credit_profiles ucp " +
            "LEFT JOIN users u ON ucp.user_id = u.id " +
            "ORDER BY ucp.current_score DESC, ucp.reward_points DESC " +
            "LIMIT #{limit}")
    List<UserCreditProfile> getCreditRankingWithUserName(Integer limit);
    
    /**
     * 分页查询用户信用档案列表（包含用户信息）
     * 
     * @param offset 偏移量
     * @param size 每页大小
     * @param keyword 关键词
     * @param creditLevel 信用等级
     * @return 档案列表
     */
    @Select("<script>" +
            "SELECT ucp.*, u.username, u.real_name, u.phone, u.id_card " +
            "FROM user_credit_profiles ucp " +
            "LEFT JOIN users u ON ucp.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword.trim() != \"\"'>" +
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='creditLevel != null and creditLevel.trim() != \"\"'>" +
            "AND ucp.credit_level = #{creditLevel} " +
            "</if>" +
            "ORDER BY ucp.current_score DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<UserCreditProfile> getUserCreditProfileListWithUserInfo(Integer offset, Integer size, String keyword, String creditLevel);
    
    /**
     * 获取用户信用档案总数
     * 
     * @param keyword 关键词
     * @param creditLevel 信用等级
     * @return 总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM user_credit_profiles ucp " +
            "LEFT JOIN users u ON ucp.user_id = u.id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword.trim() != \"\"'>" +
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='creditLevel != null and creditLevel.trim() != \"\"'>" +
            "AND ucp.credit_level = #{creditLevel} " +
            "</if>" +
            "</script>")
    Long getUserCreditProfileCount(String keyword, String creditLevel);
} 