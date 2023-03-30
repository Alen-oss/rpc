package com.wtgroup.rpccore.handler;

import com.wtgroup.rpccore.common.MsgStatusEnum;
import com.wtgroup.rpccore.common.MsgTypeEnum;
import com.wtgroup.rpccore.protocol.ProtocolHeader;
import com.wtgroup.rpccore.protocol.RpcProtocol;
import com.wtgroup.rpccore.protocol.RpcRequest;
import com.wtgroup.rpccore.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SimpleChannelInboundHandler<I> extends ChannelInboundHandlerAdapter
 * 通过泛型指定接收入参的类型，只有类型匹配的请求才会进入到这个ChannelHandler中
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private ThreadPoolExecutor threadPoolExecutor;

    private HashMap<String, Object> registryServiceMap;

    public RpcRequestHandler(ThreadPoolExecutor threadPoolExecutor, HashMap<String, Object> registryServiceMap) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.registryServiceMap = registryServiceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> msg) throws Exception {
        threadPoolExecutor.submit(() -> {
            RpcProtocol<RpcResponse> rpcProtocol = new RpcProtocol<>();
            RpcResponse rpcResponse = new RpcResponse();
            ProtocolHeader protocolHeader = msg.getProtocolHeader();
            // 修改报文类型
            protocolHeader.setMsgType((byte) MsgTypeEnum.RESPONSE.getType());
            try {
                Object result = handle(msg.getBody());
                rpcResponse.setData(result);
                protocolHeader.setStatus((byte) MsgStatusEnum.SUCCESS.getCode());
                rpcProtocol.setProtocolHeader(protocolHeader);
                rpcProtocol.setBody(rpcResponse);
            } catch (Throwable throwable) {
                protocolHeader.setStatus((byte) MsgStatusEnum.FAIL.getCode());
                rpcResponse.setMessage(throwable.toString());
                log.error("process request {} error", protocolHeader.getRequestId(), throwable);
            }
            ctx.writeAndFlush(rpcProtocol);
        });
    }

    /**
     * 调用远程方法
     */
    private Object handle(RpcRequest request) throws Throwable {
        String serviceKey = String.join("#", request.getClassName(), request.getServiceVersion());
        Object serviceBean = registryServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParams();

        // cglib就是使用了FastClass实现的，这也是cglib性能高的地方
        FastClass fastClass = FastClass.create(serviceClass);
        // 寻找方法的索引值
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        // 直接调用索引值就行
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
}
