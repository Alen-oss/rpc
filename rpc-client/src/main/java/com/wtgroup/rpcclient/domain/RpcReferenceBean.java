package com.wtgroup.rpcclient.domain;

import com.wtgroup.rpcclient.proxy.RpcInvokerProxy;
import com.wtgroup.rpccore.common.RegistryTypeEnum;
import com.wtgroup.rpcregistry.RegistryServiceFactory;
import com.wtgroup.rpcregistry.service.RegistryService;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class RpcReferenceBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String serviceVersion;

    private String registryType;

    private String registryAddr;

    private long timeout;

    private Object object;

    public void init() throws Exception {

        RegistryService registryService = RegistryServiceFactory.createRegistryService(RegistryTypeEnum.valueOf(this.registryType), this.registryAddr);
        // 创建代理类（JDK动态代理）
        this.object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcInvokerProxy(serviceVersion, timeout, registryService));
    }

    @Override
    public Object getObject() throws Exception {
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }
}
