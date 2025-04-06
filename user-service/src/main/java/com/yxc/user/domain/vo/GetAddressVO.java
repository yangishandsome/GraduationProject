package com.yxc.user.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class GetAddressVO {
    private Long id;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String name;
    private String phone;
    private Boolean isDefault;
    private Boolean editing;
    private List<String> selectedOptions;
}
