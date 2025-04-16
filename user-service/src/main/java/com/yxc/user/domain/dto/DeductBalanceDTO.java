package com.yxc.user.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeductBalanceDTO {
    private Long userId;

    private BigDecimal amount;
}
