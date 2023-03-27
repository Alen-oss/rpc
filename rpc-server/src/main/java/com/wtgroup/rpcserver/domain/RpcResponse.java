package com.wtgroup.rpcserver.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息主体类对象
 */
@Data
public class RpcResponse implements Serializable {

    private Object data;

    private String message;
}
