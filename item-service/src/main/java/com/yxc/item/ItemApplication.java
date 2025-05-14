package com.yxc.item;

import com.yxc.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.yxc.api.client.fallback", "com.yxc.item"})
@MapperScan("com.yxc.item.mapper")
@EnableFeignClients(basePackages = "com.yxc.api", defaultConfiguration = DefaultFeignConfig.class)
public class ItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }
}
