package com.yxc.item.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateItemDTO {
    private Long itemId;
    private String name;
    private BigDecimal price;
    private Integer capacity;
    private String imageUrl;
}
