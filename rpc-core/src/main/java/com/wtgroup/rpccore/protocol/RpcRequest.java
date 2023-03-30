package com.wtgroup.rpccore.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息主体类对象
 */
@Data
public class RpcRequest implements Serializable {

    private String serviceVersion;

    private String className;

    private String methodName;

    private Object[] params;

    private Class<?>[] parameterTypes;
}
