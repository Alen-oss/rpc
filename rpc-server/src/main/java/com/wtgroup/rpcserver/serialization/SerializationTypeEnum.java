package com.wtgroup.rpcserver.serialization;

import lombok.Getter;

public enum SerializationTypeEnum {
    HESSIAN(0x10),
    JSON(0x20),
    KRYO(0x30);

    @Getter
    private int type;

    SerializationTypeEnum(int type) {
        this.type = type;
    }

    public static SerializationTypeEnum findByType(byte serializationType) {
        switch (serializationType) {
            // 0x代表16进制，固定前缀。0x10代表10进制中的16
            case 0x10:
                return SerializationTypeEnum.HESSIAN;
            // 代表10进制中的32
            case 0x20:
                return SerializationTypeEnum.JSON;
            case 0x30:
                return SerializationTypeEnum.KRYO;
            default:
                throw new IllegalArgumentException("serialization type is illegal, " + serializationType);
        }
    }
}
