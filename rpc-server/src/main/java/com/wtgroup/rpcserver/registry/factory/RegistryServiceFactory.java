package com.wtgroup.rpcserver.registry.factory;

import com.wtgroup.rpcserver.registry.meta.RegistryTypeEnum;
import com.wtgroup.rpcserver.registry.RegistryService;
import com.wtgroup.rpcserver.registry.impl.EurekaRegistryService;
import com.wtgroup.rpcserver.registry.impl.ZookeeperRegistryService;

/**
 * 简单工厂模式
 * 单例模式/饿汉模式/双重检查
 */
public class RegistryServiceFactory {

    // volatile保证了registryService对象创建的有序性：1）分配内存空间 2）调用对象构造器生成对象，初始化 3）将对象引用赋值给变量
    private static volatile RegistryService registryService;

    public static RegistryService createRegistryService(RegistryTypeEnum registryTypeEnum, String registryAddr) {

        if (registryService == null) {
            synchronized(RegistryServiceFactory.class) {
                if (registryService == null) {
                    switch (registryTypeEnum) {
                        case EUREKA:
                            registryService = new EurekaRegistryService();
                            break;
                        case ZOOKEEPER:
                            registryService = new ZookeeperRegistryService();
                            break;
                        // 这里其实没有必要写默认值，因为入参为枚举类，本身就保证了数据的安全性，但为了代码结构的健全性还是写上好
                        default:
                            break;
                    }
                }
            }
        }
        return registryService;
    }
}
