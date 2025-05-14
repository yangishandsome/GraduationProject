package com.yxc.api.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yxc.api.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@TableName(value = "orders")
public class Order {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "order_id")
    private Long orderId;

    private Long userId;

    private Long itemId;

    private String itemName;

    private String itemImageUrl;

    private Integer days;

    @TableField("status")
    private OrderStatus status;

    private String addressDetail;

    private String receiverName;

    private String contactPhone;

    private LocalDate startTime;

    private LocalDate expectedEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date actualEnd;

    private BigDecimal totalFee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updatedAt;
}
