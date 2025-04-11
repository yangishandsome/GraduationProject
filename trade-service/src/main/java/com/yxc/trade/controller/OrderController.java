package com.yxc.trade.controller;

import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.common.domain.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private ItemClient itemClient;

    @GetMapping("/test")
    private Result<List<Item>> test() {
        return Result.ok(itemClient.getItemByIds(List.of(4L, 6L, 10L)));
    }
}
