package com.yxc.item;

import com.yxc.item.domain.po.Item;
import com.yxc.item.service.ItemService;
import com.yxc.item.utils.AliOssUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
class ItemServiceApplicationTests {

	@Resource
	private AliOssUtil aliOssUtil;
	@Resource
	private ItemService itemService;

	@Test
	void contextLoads() {
		try {
			FileInputStream inputStream = new FileInputStream("D:\\Mycode\\vscode\\vue_project\\battery-rent\\src\\images\\igor-omilaev-73taIS3YeNQ-unsplash.jpg");
			byte[] bytes = inputStream.readAllBytes();
			aliOssUtil.upload(bytes, UUID.randomUUID() + "test.jpg");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void test() {
		Item item = new Item();
		item.setName("超威一号");
		item.setCapacity(100);
		item.setPrice(new BigDecimal("1.02"));
		item.setImageUrl("https://yxc-bucket-001.oss-cn-shanghai.aliyuncs.com/2154221e-0241-47af-8b3f-b41a9f30be5ftest.jpg");
		itemService.save(item);
	}

}
