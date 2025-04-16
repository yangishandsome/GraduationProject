package com.yxc.trade;

import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class TradeServiceApplicationTests {

	@Resource
	private OrderService orderService;

	@Resource
	private ItemClient itemClient;

	@Test
	void test() {
		List<Item> items = itemClient.getItemByIds(List.of(4L, 5L, 6L, 7L));
		Map<Long, String> map = new HashMap<>();
		for (Item item : items) {
			map.put(item.getItemId(), item.getImageUrl());
		}
		List<Order> orders = orderService.lambdaQuery().list();
		for (Order order : orders) {
			order.setItemImageUrl(map.get(order.getItemId()));
		}
		orderService.updateBatchById(orders);
	}

}
