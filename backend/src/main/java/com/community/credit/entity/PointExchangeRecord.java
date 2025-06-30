package com.community.credit.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 积分兑换记录实体类
 * 
 * @author CommunityCredit
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("point_exchange_records")
@Schema(name = "PointExchangeRecord", description = "积分兑换记录")
public class PointExchangeRecord {

    @Schema(description = "兑换记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "商品ID")
    @TableField("product_id")
    private Integer productId;

    @Schema(description = "商品名称")
    @TableField("product_name")
    private String productName;

    @Schema(description = "使用积分")
    @TableField("points_used")
    private Integer pointsUsed;

    @Schema(description = "兑换数量")
    @TableField("quantity")
    private Integer quantity;

    @Schema(description = "兑换状态")
    @TableField("status")
    private ExchangeStatus status;

    @Schema(description = "兑换时间")
    @TableField("exchange_time")
    private LocalDateTime exchangeTime;

    @Schema(description = "备注")
    @TableField("remarks")
    private String remarks;

    /**
     * 兑换状态枚举
     */
    public enum ExchangeStatus {
        SUCCESS("SUCCESS", "成功"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String description;

        ExchangeStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
} 