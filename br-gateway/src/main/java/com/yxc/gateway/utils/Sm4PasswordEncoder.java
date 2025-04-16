package com.yxc.gateway.utils;

import cn.hutool.crypto.SmUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Sm4PasswordEncoder implements PasswordEncoder {

    private final String KEY = "NameXinChengYang";

    @Override
    public String encode(CharSequence rawPassword) {
        return SmUtil.sm4(KEY.getBytes(StandardCharsets.UTF_8)).encryptHex(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return Objects.equals(rawPassword.toString(),
                SmUtil.sm4(KEY.getBytes(StandardCharsets.UTF_8)).decryptStr(encodedPassword, StandardCharsets.UTF_8));
    }
}
