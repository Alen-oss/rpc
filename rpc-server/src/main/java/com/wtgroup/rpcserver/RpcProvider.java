package com.wtgroup.rpcserver;

import com.wtgroup.rpccore.handler.RpcDecoder;
import com.wtgroup.rpccore.handler.RpcEncoder;
import com.wtgroup.rpccore.handler.RpcRequestHandler;
import com.wtgroup.rpcregistry.meta.ServiceMeta;
import com.wtgroup.rpcregistry.service.RegistryService;
import com.wtgroup.rpcserver.annotation.RpcService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作为RPC服务的提供者，即服务方
 */
@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    // 提供RPC服务的地址，即本机地址
    private String address;
    // 提供的服务端口
    private int port;
    // 负责注册RPC服务的接口实现类
    private RegistryService registryService;
    // 内存中缓存RPC服务映射表
    private final HashMap<String, Object> rpcServiceMap = new HashMap<>();
    // 线程池
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));

    public RpcProvider(int port, RegistryService registryService) {
        this.port = port;
        this.registryService = registryService;
    }

    /**
     * 这个方法将会在bean初始化后执行（只针对实现类的bean对象）
     * bean对象的生命周期一般分为5部分：
     * 1）实例化（此处为给bean对象分配内存地址）
     * 2）设置属性（这部分是对bean属性和依赖的设置，可以看成是赋默认值）
     * 3）初始化
     * 4）使用
     * 5）销毁
     */
    @Override
    public void afterPropertiesSet() {

        new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server fail.", e);
            }
        }).start();
    }

    /**
     * 在bean初始化之后执行（所有bean在初始化后都会执行的方法）
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 通过反射拿到Class对象，获取Class对象的特定注解
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        // 类上如果没有这个注解，则直接退出
        if (rpcService == null) {
            return bean;
        }
        // 获取服务接口名称
        String serviceName = rpcService.serviceInterface().getName();
        // 获取RPC版本号
        String serviceVersion = rpcService.serviceVersion();
        try {
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceName(serviceName);
            serviceMeta.setServiceVersion(serviceVersion);
            serviceMeta.setServiceAddr(this.address);
            serviceMeta.setServicePort(this.port);
            // 注册RPC服务
            registryService.registry(serviceMeta);
            rpcServiceMap.put(String.join("#", serviceMeta.getServiceName(), serviceMeta.getServiceVersion()), bean);
        } catch (Exception e) {
            log.info("failed to register service {}#{}", serviceName, serviceVersion, e);
        }
        return bean;
    }

    /**
     * 服务启动方法
     */
    private void startRpcServer() throws Exception {

        // 获取本地的IP地址
        this.address = InetAddress.getLocalHost().getHostAddress();
        // boss线程组：负责监听端口请求，创建新连接
        EventLoopGroup boss = new NioEventLoopGroup();
        // worker线程组：负责对已经建立连接的请求进行数据的读写
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            // 服务端的引导类，这个类将引导服务端的启动工作
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    // 指定服务端的IO模型：NIO
                    .channel(NioServerSocketChannel.class)
                    // 自定义服务端在启动过程中的一些逻辑，一般情况下不用这个方法的
                    .handler(new ChannelInitializer<NioServerSocketChannel>() {
                        protected void initChannel(NioServerSocketChannel ch) {
                            System.out.println("服务端正在启动...");
                        }
                    })
                    // 开始定义每个连接的读写
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcRequestHandler(threadPoolExecutor, rpcServiceMap))
                                    .addLast(new RpcEncoder());
                        }
                    })
                    // 给每个连接设置一些TCP的参数，这里开启了探活机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // bind：绑定服务端口，这个方法是一个异步方法
            ChannelFuture channelFuture = bootstrap.bind(this.address, this.port).sync();
            log.info("server address {}, started on port {}", this.address, this.port);
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * 绑定端口（流程中没有用到，自己写着玩的）
     * 即如果port被占用，则自增+1向上寻找，直到成功；正常逻辑中端口被占用直接报错即可
     */
    private ChannelFuture bind(ServerBootstrap bootstrap, String address, int port) throws InterruptedException {
        // 这里就是给bind的返回值ChannelFuture加个监听器
        ChannelFuture future = bootstrap.bind(address, port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    log.info("server address {}, port {}", address, port);
                } else {
                    log.info("port {} 已经被占用", port);
                    bind(bootstrap, address, port + 1);
                }
            }
        }).sync();
        return future;
    }

}
