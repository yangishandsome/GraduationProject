package com.yxc.trade.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTurnoverVO {
    private List<BigDecimal> turnovers;

    private List<Long> totalOrders;

    private List<Long> effectiveOrders;

    private List<ItemSaleVO> itemSales;
}
