package com.yxc.common.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.autoconfigure.RateLimiterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Configuration
public class RateControlConfig {
    
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.of(
            RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(100)  // 每秒100请求
                .timeoutDuration(Duration.ofMillis(500))
                .build()
        );
    }

    @Bean
    @Primary
    public RateLimiterProperties rateLimiterProperties() {
        return new RateLimiterProperties();
    }
}