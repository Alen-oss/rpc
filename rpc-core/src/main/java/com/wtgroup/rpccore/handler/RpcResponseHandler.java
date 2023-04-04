package com.wtgroup.rpccore.handler;

import com.wtgroup.rpccore.protocol.RpcFuture;
import com.wtgroup.rpccore.protocol.RpcProtocol;
import com.wtgroup.rpccore.protocol.RpcRequestHolder;
import com.wtgroup.rpccore.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) throws Exception {

        long requestId = msg.getProtocolHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);
        // 接收返回值，设置结果：Promise<V> setSuccess(V result);
        future.getPromise().setSuccess(msg.getBody());
    }
}
