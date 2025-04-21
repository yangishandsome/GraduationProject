package com.yxc.api.client;

import com.yxc.api.client.fallback.ItemClientFallbackFactory;
import com.yxc.api.config.DefaultFeignConfig;
import com.yxc.api.po.Item;
import com.yxc.api.po.DeductStock;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "https://item-service",
        configuration = DefaultFeignConfig.class,
        fallbackFactory = ItemClientFallbackFactory.class)
public interface ItemClient {
    @GetMapping("/item/getByIds")
    List<Item> getItemByIds(@RequestParam("ids")Collection<Long> ids);

    @PutMapping("/item/deductStock")
    void deductStock(@RequestBody List<DeductStock> items);
}
