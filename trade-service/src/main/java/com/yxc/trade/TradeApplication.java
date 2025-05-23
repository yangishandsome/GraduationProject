package com.yxc.trade;

import com.yxc.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@MapperScan(value = "com.yxc.trade.mapper")
@EnableFeignClients(basePackages = "com.yxc.api", defaultConfiguration = DefaultFeignConfig.class)
@EnableScheduling
public class TradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }
}
