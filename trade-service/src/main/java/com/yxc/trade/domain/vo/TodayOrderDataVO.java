package com.yxc.trade.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodayOrderDataVO {

    private BigDecimal turnover;

    private Long effectiveOrder;

    private BigDecimal orderCompleteRate;

    private BigDecimal avgOrderPrice;
}
