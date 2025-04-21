package com.yxc.trade.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisagreeRefundDTO {
    private Long orderId;

    private String disagreeReason;
}
