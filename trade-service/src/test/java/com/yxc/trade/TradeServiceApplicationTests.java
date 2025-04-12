package com.yxc.trade;

import com.yxc.api.client.ItemClient;
import com.yxc.api.po.OrderDetail;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
class TradeServiceApplicationTests {

	@Resource
	private OrderService orderService;

	@Resource
	private ItemClient itemClient;

	@Test
	@GlobalTransactional
	void contextLoads() {
	}

}
