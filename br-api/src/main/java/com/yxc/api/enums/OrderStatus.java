package com.yxc.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OrderStatus {
    /**
     * 订单已创建，等待付款
     */
    CREATED(0, "CREATED"),

    /**
     * 付款成功，等待发货
     */
    PAID(1, "PAID"),

    /**
     * 订单已发货
     */
    SHIPPED(2, "SHIPPED"),

    /**
     * 租赁进行中
     */
    RENTING(3, "RENTING"),

    /**
     * 租赁已超时
     */
    TIMEOUT(4, "TIMEOUT"),

    /**
     * 订单归还中
     */
    RETURNING(5, "RETURNING"),

    /**
     * 退款中
     */
    REFUNDING(6, "REFUNDING"),

    /**
     * 商品已归还
     */
    RETURNED(7, "RETURNED"),


    /**
     * 退款完成
     */
    REFUNDED(8, "REFUNDED"),

    /**
     * 订单已取消
     */
    CANCELLED(9, "CANCELLED");


    @EnumValue
    private final Integer status;
    @JsonValue
    private final String description;

    OrderStatus(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

}