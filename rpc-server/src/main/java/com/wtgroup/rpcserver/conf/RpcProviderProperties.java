package com.wtgroup.rpcserver.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
// 自动装配配置文件属性
@ConfigurationProperties(prefix = "rpc")
public class RpcProviderProperties {

    private int port;

    private String registerAddr;

    private String registerType;
}
