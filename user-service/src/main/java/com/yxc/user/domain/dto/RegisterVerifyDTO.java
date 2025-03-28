package com.yxc.user.domain.dto;

import lombok.Data;

@Data
public class RegisterVerifyDTO {
    private String phone;

    private String code;
}
