package com.yxc.trade.domain.dto;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long itemId;

    private Integer days;

    private String addressDetail;

    private String receiverName;

    private String contactPhone;
}
