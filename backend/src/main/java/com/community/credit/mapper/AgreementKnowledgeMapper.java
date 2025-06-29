package com.community.credit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.credit.entity.AgreementKnowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公约知识库Mapper接口
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Mapper
public interface AgreementKnowledgeMapper extends BaseMapper<AgreementKnowledge> {

    /**
     * 根据关键词搜索知识库
     * 
     * @param keyword 关键词
     * @return 匹配的知识库列表
     */
    @Select("SELECT * FROM agreement_knowledge " +
            "WHERE is_active = 1 " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%') " +
            "OR keywords LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY created_time DESC")
    List<AgreementKnowledge> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据公约ID删除知识库记录
     * 
     * @param agreementId 公约ID
     * @return 影响行数
     */
    @Select("DELETE FROM agreement_knowledge WHERE agreement_id = #{agreementId}")
    int deleteByAgreementId(@Param("agreementId") Integer agreementId);

    /**
     * 获取所有启用的知识库内容
     * 
     * @return 知识库列表
     */
    @Select("SELECT * FROM agreement_knowledge WHERE is_active = 1 ORDER BY created_time DESC")
    List<AgreementKnowledge> getAllActiveKnowledge();
} 