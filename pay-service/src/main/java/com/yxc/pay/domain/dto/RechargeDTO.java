package com.yxc.pay.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RechargeDTO {
    private BigDecimal amount;

    private String paymentMethod;
}
