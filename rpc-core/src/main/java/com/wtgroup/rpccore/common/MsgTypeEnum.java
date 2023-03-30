package com.wtgroup.rpccore.common;

import lombok.Getter;

/**
 * 协议报文类型枚举类
 */
@Getter
public enum MsgTypeEnum {

    REQUEST(1),
    RESPONSE(2),
    HEARTBEAT(3);

    private int type;
    MsgTypeEnum(int type) {
        this.type = type;
    }

    public static MsgTypeEnum findByType(int type) {

        switch (type) {
            case 1:
                return MsgTypeEnum.REQUEST;
            case 2:
                return MsgTypeEnum.RESPONSE;
            case 3:
                return MsgTypeEnum.HEARTBEAT;
            default:
                throw new IllegalArgumentException("msg type is illegal, " + type);
        }
    }
}
