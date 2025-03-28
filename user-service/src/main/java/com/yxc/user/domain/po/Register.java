package com.yxc.user.domain.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class Register implements Serializable {
    private String username;
    private String phone;
    private String password;
}
