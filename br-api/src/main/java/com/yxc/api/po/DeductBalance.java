package com.yxc.api.po;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeductBalance {
    private Long userId;

    private BigDecimal amount;
}
