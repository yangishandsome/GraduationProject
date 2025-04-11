package com.yxc.user.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddBalanceDTO {
    private BigDecimal amount;
    private String payMethod;
}
