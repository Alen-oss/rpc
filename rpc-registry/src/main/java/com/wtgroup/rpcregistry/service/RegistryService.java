package com.wtgroup.rpcregistry.service;

import com.wtgroup.rpcregistry.meta.ServiceMeta;

import java.io.IOException;

/**
 * 服务注册的接口
 */
public interface RegistryService {

    void registry(ServiceMeta serviceMeta) throws Exception;

    void unRegistry(ServiceMeta serviceMeta) throws Exception;

    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    void destroy() throws IOException;

}
