package com.wtgroup.rpccore.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RpcRequestHolder {

    // 生成request_id
    public static final AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    // 存储request和response的对应关系
    public static final Map<Long, RpcFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
