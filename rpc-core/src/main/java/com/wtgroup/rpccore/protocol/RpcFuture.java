package com.wtgroup.rpccore.protocol;

import io.netty.util.concurrent.Promise;
import lombok.Data;

/**
 * 这里讲讲JDK的Future、Netty的Future、Netty的Promise
 *
 * 首先，JDK提供的Future并不算真正的异步，因为它少了一个回调，充其量算一个同步非阻塞模式，Future.get()还是会阻塞的
 * 获取的带返回值的Future有两种方式：1）实现了Callable接口 2）继承了Future的子类：FutureTask（FutureTask本质上也是使用了Callable进行创建的）
 * 所以说,实际上就一种：即实现Callable接口，这也是Callable和Runnable的区别：Callable可以在任务结束后提供一个返回值，而Runnable不可以；此外call方法是允许抛出异常的，run方法是不可以的
 * Java 8中新增了一个类：CompletableFuture，它提供了非常强大的Future的扩展功能，最重要的是实现了回调的功能。
 * 用法之一：CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {...return "results"}, executorService)，当然还有很有用法，可以自己去看
 *
 * Netty的Future继承了JDK的Future，我们常用的ChannelFuture则继承了Netty的Future。我们常用的方法：
 * 1）sync（等待future done）2）isSuccess（只有操作完成时才会返回true）3)addListener（添加监听器）
 * Netty中引入了Promise机制，在Promise机制中，可以在业务逻辑中人工设置业务逻辑的成功与失败，这样更加方便监控自己的业务逻辑
 *
 * Promise（官方注释：特殊的可写的）继承了Future，实现类为DefaultPromise类。脱离了任务独立存在，只作为两个线程之间传递结果的容器（你可以把它看成一个结果传递的容器）。
 */
@Data
public class RpcFuture<T> {

    private Promise<T> promise;

    private Long timeout;

    public RpcFuture(Promise<T> promise, long timeout) {
        this.promise = promise;
        this.timeout = timeout;
    }
}
