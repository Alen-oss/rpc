package com.wtgroup.rpcregistry.meta;

import lombok.Data;

/**
 * @RpcService元数据，用于存储注解信息，注册服务
 */
// 简明代码，自动生成get/set方法
@Data
public class ServiceMeta {

    private String serviceName;

    private String serviceVersion;

    private String serviceAddr;

    private int servicePort;
}
