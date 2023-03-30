package com.wtgroup.rpcserver.facade;

import com.wtgroup.rpcfacade.HelloFacade;
import com.wtgroup.rpcserver.annotation.RpcService;

@RpcService(serviceInterface = HelloFacade.class, serviceVersion = "1.0.0")
public class HelloFacadeImpl implements HelloFacade {

    @Override
    public String hello(String name) {
        return "hello, " + name;
    }
}
