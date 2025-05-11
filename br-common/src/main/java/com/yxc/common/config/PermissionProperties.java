package com.yxc.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "br.permission")
public class PermissionProperties {
    private List<String> level1Paths;
    private List<String> level2Paths;
    private List<String> level3Paths;
}
