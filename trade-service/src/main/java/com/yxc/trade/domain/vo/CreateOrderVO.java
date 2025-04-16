package com.yxc.trade.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yxc.trade.domain.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class CreateOrderVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    private String itemName;

    private Integer days;

    private OrderStatus status;

    private BigDecimal totalFee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createdAt;
}
