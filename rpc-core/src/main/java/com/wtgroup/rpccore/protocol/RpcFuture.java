package com.wtgroup.rpccore.protocol;

import io.netty.util.concurrent.Promise;
import lombok.Data;

@Data
public class RpcFuture<T> {

    private Promise<T> promise;

    private Long timeout;

    public RpcFuture(Promise<T> promise, long timeout) {
        this.promise = promise;
        this.timeout = timeout;
    }
}
