package com.yxc.trade.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

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
     * 商品已归还
     */
    RETURNED(5, "RETURNED"),

    /**
     * 订单已取消
     */
    CANCELLED(6, "CANCELLED"),

    /**
     * 退款中
     */
    REFUNDING(7, "REFUNDING"),

    /**
     * 退款完成
     */
    REFUNDED(8, "REFUNDED");

    @Getter
    @EnumValue
    private final Integer status;
    @Getter
    @JsonValue
    private final String description;

    OrderStatus(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

}