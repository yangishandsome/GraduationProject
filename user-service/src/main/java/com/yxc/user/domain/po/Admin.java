package com.yxc.user.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@TableName("admins")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Admin {
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long adminId;

    private String passwordHash;

    private String username;

    private Integer permission;

    private Short status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createdAt;
}
