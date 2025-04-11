package com.yxc.api.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Item {
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
