package com.wtgroup.rpcserver.domain.protocol;

import lombok.Getter;

@Getter
public enum MsgStatusEnum {
    SUCCESS(0),
    FAIL(1);

    private final int code;

    MsgStatusEnum(int code) {
        this.code = code;
    }
}
