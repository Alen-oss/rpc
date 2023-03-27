package com.wtgroup.rpcserver.handler;

import com.wtgroup.rpcserver.common.Constants;
import com.wtgroup.rpcserver.domain.RpcRequest;
import com.wtgroup.rpcserver.domain.RpcResponse;
import com.wtgroup.rpcserver.domain.protocol.MsgTypeEnum;
import com.wtgroup.rpcserver.domain.protocol.ProtocolHeader;
import com.wtgroup.rpcserver.domain.protocol.RpcProtocol;
import com.wtgroup.rpcserver.serialization.RpcSerialization;
import com.wtgroup.rpcserver.serialization.SerializationTypeEnum;
import com.wtgroup.rpcserver.serialization.factory.SerializationFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * ByteToMessageDecoder extends ChannelInboundHandlerAdapter
 * 实现decode抽象方法即可，入参的in已经是ByteBuf，不需要我们再操作将字节流写入缓冲区了，并且会自动释放内存
 * out.add()负责将字节流转化成的java对象输出到下一个ChannelHandler中
 * 这个类省去了我们很多的工作量，是Netty提供的几个经典ChannelInboundHandler
 */
public class RpcDecoder extends ByteToMessageDecoder {

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
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() < Constants.HEADER_TOTAL_LEN) {
            return;
        }
        // 标记readerIndex读指针，后面可以通过调用resetReaderIndex()方法回到标记位
        in.markReaderIndex();

        short magic = in.readShort();
        if (magic != Constants.MAGIC) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte version = in.readByte();
        byte serializeType = in.readByte();
        byte msgType = in.readByte();
        byte status = in.readByte();
        long requestId = in.readLong();
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        // 转化成相应枚举类，并对参数进行校验，非法则抛出IllegalArgumentException
        MsgTypeEnum msgTypeEnum = MsgTypeEnum.findByType(msgType);
        SerializationTypeEnum serializationTypeEnum = SerializationTypeEnum.findByType(serializeType);

        ProtocolHeader header = new ProtocolHeader();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerialization(serializeType);
        header.setMsgType(msgType);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMsgLen(dataLength);

        RpcSerialization rpcSerialization = SerializationFactory.getInstance(serializationTypeEnum);
        switch (msgTypeEnum) {
            case REQUEST:
                RpcRequest rpcRequest = rpcSerialization.deserialize(data, RpcRequest.class);
                if (rpcRequest != null) {
                    RpcProtocol<RpcRequest> rpcProtocol = new RpcProtocol<>();
                    rpcProtocol.setProtocolHeader(header);
                    rpcProtocol.setBody(rpcRequest);
                    out.add(rpcProtocol);
                }
                break;
            case RESPONSE:
                RpcResponse rpcResponse = rpcSerialization.deserialize(data, RpcResponse.class);
                if (rpcResponse != null) {
                    RpcProtocol<RpcResponse> rpcProtocol = new RpcProtocol<>();
                    rpcProtocol.setProtocolHeader(header);
                    rpcProtocol.setBody(rpcResponse);
                    out.add(rpcProtocol);
                }
                break;
            case HEARTBEAT:
                // TODO
                break;

        }

    }
}
