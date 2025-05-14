package com.yxc.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @JsonSerialize(using = ToStringSerializer.class)
    @TableId(value = "item_id")
    private Long itemId;

    private String name;

    private Integer capacity;

    private String imageUrl;

    private BigDecimal price;

    private Short currentHealth;

    private Short currentCharge;

    private Short status;

    private LocalDateTime lastMaintained;

}
