package com.wtgroup.rpcserver.handler;

import com.wtgroup.rpcserver.domain.protocol.ProtocolHeader;
import com.wtgroup.rpcserver.domain.protocol.RpcProtocol;
import com.wtgroup.rpcserver.serialization.RpcSerialization;
import com.wtgroup.rpcserver.serialization.SerializationTypeEnum;
import com.wtgroup.rpcserver.serialization.factory.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    /*
    +---------------------------------------------------------------+
    | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
    +---------------------------------------------------------------+
    | 状态 1byte |        消息 ID 8byte     |      数据长度 4byte     |
    +---------------------------------------------------------------+
    |                   数据内容 （长度不定）                          |
    +---------------------------------------------------------------+
    */

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol<Object> msg, ByteBuf out) throws Exception {

        ProtocolHeader header = msg.getProtocolHeader();
        out.writeShort(header.getMagic());
        out.writeByte(header.getVersion());
        out.writeByte(header.getSerialization());
        out.writeByte(header.getMsgType());
        out.writeByte(header.getStatus());
        out.writeLong(header.getRequestId());
        RpcSerialization rpcSerialization = SerializationFactory.getInstance(SerializationTypeEnum.findByType(header.getSerialization()));
        byte[] data = rpcSerialization.serialize(msg.getBody());
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
