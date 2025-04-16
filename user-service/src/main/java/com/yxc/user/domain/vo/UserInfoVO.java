package com.yxc.user.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserInfoVO {
    private String username;

    private String email;

    private String phone;

    private String address;

    private BigDecimal balance;
}
