package com.wtgroup.rpccore.serialization.factory;

import com.wtgroup.rpccore.common.SerializationTypeEnum;
import com.wtgroup.rpccore.serialization.HessianSerialization;
import com.wtgroup.rpccore.serialization.JsonSerialization;
import com.wtgroup.rpccore.serialization.KryoSerialization;
import com.wtgroup.rpccore.serialization.RpcSerialization;

public class SerializationFactory {

    // 常量在类加载时就注入生成了，保证了单例
    static RpcSerialization hessianRpcSerialization = new HessianSerialization();

    static RpcSerialization kryoRpcSerialization = new KryoSerialization();

    public static RpcSerialization getInstance(SerializationTypeEnum serializationTypeEnum) {

        switch (serializationTypeEnum) {
            case JSON:
                return new JsonSerialization();
            case HESSIAN:
                return hessianRpcSerialization;
            default:
                return kryoRpcSerialization;
        }
    }
}
