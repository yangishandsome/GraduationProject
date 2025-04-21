package com.yxc.trade.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yxc.trade.domain.enums.OrderStatus;
import com.yxc.trade.domain.po.OrderDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    private String itemName;

    private String itemImageUrl;

    private String receiverName;

    private String contactPhone;

    private String addressDetail;

    private BigDecimal totalFee;

    private OrderStatus status;

    private Integer days;

    private LocalDate startTime;

    private LocalDate expectedEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date payTime;

    private List<OrderDetail> statusLogs;
}
