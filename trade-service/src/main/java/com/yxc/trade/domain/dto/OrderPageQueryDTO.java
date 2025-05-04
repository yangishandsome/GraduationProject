package com.yxc.trade.domain.dto;

import com.yxc.common.domain.PageQuery;
import com.yxc.trade.domain.enums.OrderStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderPageQueryDTO extends PageQuery {
    private List<OrderStatus> status;

    private String orderNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

}
