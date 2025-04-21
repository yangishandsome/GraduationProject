package com.yxc.gateway.filters;

import cn.hutool.json.JSONUtil;
import com.yxc.gateway.config.AuthProperties;
import com.yxc.gateway.utils.JwtTool;
import com.yxc.gateway.utils.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestPath path = request.getPath();
        if(isExclude(path.toString())) {
            log.info("请求：{} 无需认证", path);
            return chain.filter(exchange);
        }
        List<String> headers = request.getHeaders().get("token");

        String token = null;
        if(headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }

        UserInfo userInfo;
        try {
            userInfo = jwtTool.parseToken(token);
        } catch (Exception e) {
            log.info("请求：{} token解析失败", path);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", JSONUtil.toJsonStr(userInfo)))
                .build();
        log.info("请求：{} token解析成功", path);
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        for(String excludePath : excludePaths) {
            if(antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
