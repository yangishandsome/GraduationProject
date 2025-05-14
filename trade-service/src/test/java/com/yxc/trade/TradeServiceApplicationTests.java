package com.yxc.trade;

import com.yxc.api.client.ItemClient;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class TradeServiceApplicationTests {

	@Resource
	private OrderService orderService;

	@Resource
	private ItemClient itemClient;

	@Test
    public void test() {
		List<Order> orders = orderService.lambdaQuery()
				.isNotNull(Order::getActualEnd)
				.list();
		log.info("orders:{}", orders);
	}

}
