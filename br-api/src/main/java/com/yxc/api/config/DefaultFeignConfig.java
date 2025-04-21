package com.yxc.api.config;

import cn.hutool.json.JSONUtil;
import com.yxc.api.client.fallback.ItemClientFallbackFactory;
import com.yxc.api.client.fallback.UserClientFallbackFactory;
import com.yxc.common.domain.UserInfo;
import com.yxc.common.utils.UserContext;
import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;

@Slf4j
@Configuration
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            UserInfo user = UserContext.getUser();
            if(user != null) {
                String userJSON = JSONUtil.toJsonStr(user);
                requestTemplate.header("userJSON-info", userJSON);
            }
        };
    }

    @Bean
    public Client feignClient() throws Exception {
        return new Client.Default(
                getSSLSocketFactory(),
                new DefaultHostnameVerifier()
        );
    }

    private SSLSocketFactory getSSLSocketFactory() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, new TrustAllStrategy()) // 临时信任所有证书
                .build();

        return sslContext.getSocketFactory();
    }

    // 临时关闭主机名验证（仅限开发环境）
    @Bean
    public HostnameVerifier hostnameVerifier() {
        return (hostname, session) -> true;
    }

    @Bean
    public ItemClientFallbackFactory itemClientFallbackFactory() {
        return new ItemClientFallbackFactory();
    }

    @Bean
    public UserClientFallbackFactory userClientFallbackFactory() {
        return new UserClientFallbackFactory();
    }

}
