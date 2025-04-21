package com.yxc.api.client.fallback;

import com.yxc.api.client.UserClient;
import com.yxc.api.po.DeductBalance;
import com.yxc.api.po.ReturnBalance;
import com.yxc.api.po.User;
import com.yxc.common.domain.Result;
import com.yxc.common.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public List<User> getUserByIds(List<Long> ids) {
                log.info("获取用户信息失败", cause);
                return List.of();
            }

            @Override
            public Result<?> deductBalance(DeductBalance deductBalance) {
                log.info("扣减用户余额失败", cause);
                return Result.error((CommonException) cause);
            }

            @Override
            public Result<?> returnBalance(ReturnBalance returnBalance) {
                log.info("用户退款失败", cause);
                return Result.error((CommonException) cause);
            }
        };
    }
}
