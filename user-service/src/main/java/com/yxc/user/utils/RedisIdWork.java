package com.yxc.user.utils;

import cn.hutool.core.date.DateTime;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class RedisIdWork {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final long BEGIN_TIMESTAMP = 1713312000L;

    private static final int COUNT_BITS = 32;

    public Long nextId(String prefix){
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        String date = DateTime.now().toString("yyyy:MM:dd");
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + prefix + ":" + date);

        return timeStamp << COUNT_BITS | count;
    }


}
