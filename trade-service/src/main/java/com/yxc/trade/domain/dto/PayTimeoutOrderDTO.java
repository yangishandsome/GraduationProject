package com.yxc.trade.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayTimeoutOrderDTO {
    private Long orderId;

    private Integer days;

    private BigDecimal payAmount;
}
