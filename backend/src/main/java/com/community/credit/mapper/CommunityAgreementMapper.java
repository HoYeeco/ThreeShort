package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.CommunityAgreement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 社区公约Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface CommunityAgreementMapper extends BaseMapper<CommunityAgreement> {

    /**
     * 获取启用的公约列表（按排序号排序）
     * 
     * @return 公约列表
     */
    @Select("SELECT * FROM community_agreements WHERE is_active = 1 ORDER BY order_num ASC, created_time ASC")
    List<CommunityAgreement> getActiveAgreements();

    /**
     * 获取指定排序号之后的最大排序号
     * 
     * @param orderNum 排序号
     * @return 最大排序号
     */
    @Select("SELECT COALESCE(MAX(order_num), 0) FROM community_agreements WHERE order_num > #{orderNum}")
    Integer getMaxOrderAfter(Integer orderNum);

    /**
     * 获取所有公约的文本内容（用于AI问答）
     * 
     * @return 公约内容列表
     */
    @Select("SELECT id, title, content FROM community_agreements WHERE is_active = 1 ORDER BY order_num ASC")
    List<CommunityAgreement> getAgreementContents();
} 