package com.yxc.trade.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutTimeOrderInfoVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    private Integer outTimeDays;

    private BigDecimal outTimeFee;
}
