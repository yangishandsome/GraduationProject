package com.yxc.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetDefaultAddressVO {
    private Long id;

    private Long userId;

    private String name;

    private String province;

    private String city;

    private String district;

    private String detail;

    private String phone;

    private Integer isDefault;

    private LocalDateTime createAt;

    private Long count;
}
