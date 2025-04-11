package com.yxc.trade;

import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@SpringBootTest
class TradeServiceApplicationTests {

	@Resource
	private OrderService orderService;


	@Test
	void contextLoads() {
		List<Item> items = orderService.getItemByIds(List.of(4L, 6L, 10L));
		log.info("查询到商品：{}", items);
	}

}
