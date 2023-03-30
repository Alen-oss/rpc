package com.wtgroup.rpcclient.proxy;

import com.wtgroup.rpcclient.RpcClient;
import com.wtgroup.rpccore.common.Constants;
import com.wtgroup.rpccore.common.MsgTypeEnum;
import com.wtgroup.rpccore.common.SerializationTypeEnum;
import com.wtgroup.rpccore.protocol.*;
import com.wtgroup.rpcregistry.service.RegistryService;
import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;

    private final long timeout;

    private final RegistryService registryService;

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        ProtocolHeader protocolHeader = new ProtocolHeader();
        protocolHeader.setMagic(Constants.MAGIC);
        protocolHeader.setVersion(Constants.VERSION);
        protocolHeader.setMsgType((byte) MsgTypeEnum.REQUEST.getType());
        protocolHeader.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        protocolHeader.setStatus((byte) 0x1);
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        protocolHeader.setRequestId(requestId);
        protocol.setProtocolHeader(protocolHeader);

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceVersion(this.serviceVersion);
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParams(args);
        protocol.setBody(rpcRequest);

        RpcClient rpcClient = new RpcClient();
        RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        RpcRequestHolder.REQUEST_MAP.put(requestId, future);
        rpcClient.sendRequest(protocol, this.registryService);
        return future.getPromise().get(future.getTimeout(), TimeUnit.MILLISECONDS).getData();
    }
}
