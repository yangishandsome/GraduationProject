package com.yxc.trade.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayOrderDTO {
    private Long orderId;

    private BigDecimal totalFee;
}
