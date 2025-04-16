package com.yxc.api.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;

    private String phone;

    private String email;

    private String username;

    private BigDecimal balance;

    private Short status;

    private LocalDateTime createdAt;
}
