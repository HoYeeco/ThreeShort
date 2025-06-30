package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社区实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("communities")
@Schema(name = "Community", description = "社区信息")
public class Community {

    @Schema(description = "社区ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "社区名称")
    @TableField("name")
    private String name;

    @Schema(description = "社区地址")
    @TableField("address")
    private String address;

    @Schema(description = "社区描述")
    @TableField("description")
    private String description;

    @Schema(description = "管理员ID")
    @TableField("admin_id")
    private Integer adminId;

    @Schema(description = "状态：1正常 0禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
} 