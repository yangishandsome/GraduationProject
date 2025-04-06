package com.yxc.user.domain.dto;

import lombok.Data;

@Data
public class UpdateUserInfoDTO {
    private String username;
    private String email;
    private String phone;
}
