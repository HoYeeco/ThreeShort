package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公约知识库实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agreement_knowledge")
@Schema(name = "AgreementKnowledge", description = "公约知识库")
public class AgreementKnowledge {

    @Schema(description = "知识库ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "关联的公约ID")
    @TableField("agreement_id")
    private Integer agreementId;

    @Schema(description = "公约标题")
    @TableField("title")
    private String title;

    @Schema(description = "知识内容")
    @TableField("content")
    private String content;

    @Schema(description = "内容摘要")
    @TableField("summary")
    private String summary;

    @Schema(description = "关键词（用于检索）")
    @TableField("keywords")
    private String keywords;

    @Schema(description = "内容向量（用于语义检索）")
    @TableField("content_vector")
    private String contentVector;

    @Schema(description = "是否启用")
    @TableField("is_active")
    private Boolean isActive;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
} 