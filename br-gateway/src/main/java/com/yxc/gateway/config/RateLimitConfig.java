package com.yxc.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfig {
    
    // 限流维度：按用户IP
    @Bean
    KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }

    // Redis限流器配置（每秒100请求）
    @Bean
    RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200);
    }
}