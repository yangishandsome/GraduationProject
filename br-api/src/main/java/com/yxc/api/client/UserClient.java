package com.yxc.api.client;

import com.yxc.api.client.fallback.UserClientFallbackFactory;
import com.yxc.api.config.DefaultFeignConfig;
import com.yxc.api.po.DeductBalance;
import com.yxc.api.po.ReturnBalance;
import com.yxc.api.po.User;
import com.yxc.common.domain.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service",
        url = "https://localhost:8083",
        configuration = DefaultFeignConfig.class,
        fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    @GetMapping("/user/getUserByIds")
    List<User> getUserByIds(@RequestParam("ids")List<Long> ids);

    @PutMapping("/balance/deduct")
    Result<?> deductBalance(@RequestBody DeductBalance deductBalance);

    @PutMapping("/balance/return")
    Result<?> returnBalance(@RequestBody ReturnBalance returnBalance);
}
