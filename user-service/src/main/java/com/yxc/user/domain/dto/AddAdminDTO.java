package com.yxc.user.domain.dto;

import lombok.Data;

@Data
public class AddAdminDTO {
    private String username;

    private String password;

    private Integer permission;
}
