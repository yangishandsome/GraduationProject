package com.yxc.user.domain.vo;

import lombok.Data;

@Data
public class LoginVO {
    private Long userId;

    private String token;

    private String username;
}
