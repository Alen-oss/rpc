package com.wtgroup.rpcserver.conf;

import com.wtgroup.rpccore.common.RegistryTypeEnum;
import com.wtgroup.rpcregistry.RegistryServiceFactory;
import com.wtgroup.rpcregistry.service.RegistryService;
import com.wtgroup.rpcserver.RpcProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 让Spring能扫描到被@ConfigurationProperties标注的类（让被@ConfigurationProperties标注的类生效）
@EnableConfigurationProperties(RpcProviderProperties.class)
public class RpcProviderAutoConfiguration {

    @Autowired
    private RpcProviderProperties properties;

    @Bean
    public RpcProvider createRpcProvider() {
        // 枚举类自带的valueOf()方法会将与枚举类元素相同名称的字符串转化为枚举类中的对应元素
        RegistryTypeEnum registryTypeEnum = RegistryTypeEnum.valueOf(properties.getRegisterType());
        RegistryService registryService = RegistryServiceFactory.createRegistryService(registryTypeEnum, properties.getRegisterAddr());
        return new RpcProvider(properties.getPort(), registryService);
    }
}
