package com.yxc.user.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddOrUpdateAddressDTO {
    private Long id;
    private List<String> locationInfo;
    private String detail;
    private String name;
    private String phone;
    private Boolean isDefault;
}
