package com.yxc.trade.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCountByStatusVO {
    private Long total;
    private Long paid;
    private Long renting;
    private Long timeout;
    private Long refunded;
    private Long canceled;
}
