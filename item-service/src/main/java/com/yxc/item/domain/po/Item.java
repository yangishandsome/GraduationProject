package com.yxc.item.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
@EqualsAndHashCode
@Accessors(chain = true)
public class Item {
    @TableId(value = "item_id", type = IdType.AUTO)
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
