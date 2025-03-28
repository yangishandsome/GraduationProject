package com.yxc.item.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaveItemDTO {
    private String name;
    private BigDecimal price;
    private Integer capacity;
    private String imageUrl;
}
