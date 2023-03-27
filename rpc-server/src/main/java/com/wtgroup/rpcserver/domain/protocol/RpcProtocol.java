package com.wtgroup.rpcserver.domain.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 完整的协议类对象：协议头 + body
 * @param <T>
 */
@Data
public class RpcProtocol<T> implements Serializable {

    // 协议头
    private ProtocolHeader protocolHeader;
    // body
    private T body;
}
