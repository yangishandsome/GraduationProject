package com.yxc.user.domain.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;

    private String phone;

    private String password;
}
