package com.wtgroup.rpcregistry.service;


import com.wtgroup.rpcregistry.meta.ServiceMeta;

import java.io.IOException;

public class ZookeeperRegistryService implements RegistryService {

    @Override
    public void registry(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public void unRegistry(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        return null;
    }

    @Override
    public void destroy() throws IOException {

    }
}
