package com.wtgroup.rpcserver.registry;

import com.wtgroup.rpcserver.domain.ServiceMeta;

/**
 * 服务注册的接口
 */
public interface RegistryService {

    void registry(ServiceMeta serviceMeta) throws Exception;

    void unRegistry(ServiceMeta serviceMeta) throws Exception;

}
