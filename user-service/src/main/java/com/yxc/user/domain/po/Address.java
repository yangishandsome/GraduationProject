package com.yxc.user.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "address")
public class Address {
    @TableId(value = "id", type = IdType.AUTO)
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
}
