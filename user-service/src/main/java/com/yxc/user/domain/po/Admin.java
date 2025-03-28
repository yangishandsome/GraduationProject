package com.yxc.user.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("admins")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Admin {
    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long adminId;

    private String passwordHash;

    private String username;

    private Short authorization;

    private Short status;

    private LocalDateTime createdAt;
}
