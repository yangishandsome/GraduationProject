package com.yxc.trade.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yxc.trade.domain.enums.OrderStatus;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_detail")
public class OrderDetail {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private OrderStatus status;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createdAt;
}
