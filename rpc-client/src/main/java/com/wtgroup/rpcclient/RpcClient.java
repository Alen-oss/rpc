package com.wtgroup.rpcclient;

import com.wtgroup.rpccore.handler.RpcDecoder;
import com.wtgroup.rpccore.handler.RpcEncoder;
import com.wtgroup.rpccore.handler.RpcResponseHandler;
import com.wtgroup.rpccore.protocol.RpcProtocol;
import com.wtgroup.rpccore.protocol.RpcRequest;
import com.wtgroup.rpcregistry.meta.ServiceMeta;
import com.wtgroup.rpcregistry.service.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public RpcClient() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    public void sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {

        RpcRequest rpcRequest = protocol.getBody();
        Object[] params = rpcRequest.getParams();
        String serviceKey = String.join("#", rpcRequest.getClassName(), rpcRequest.getServiceVersion());
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        // 这里的服务元数据皆为被请求方服务ip + port
        ServiceMeta serviceMeta = registryService.discovery(serviceKey, invokerHashCode);
        if (serviceMeta == null) {
            return;
        }
        ChannelFuture future = bootstrap.connect(serviceMeta.getServiceAddr(), serviceMeta.getServicePort()).sync();
        future.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                } else {
                    log.info("connect rpc server {} on port {} failed.", serviceMeta.getServiceAddr(), serviceMeta.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            }
        });
        future.channel().writeAndFlush(protocol);
    }

}
