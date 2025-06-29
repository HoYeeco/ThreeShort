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
} 