package com.wtgroup.rpccore.serialization;

import java.io.IOException;

/**
 * 序列化接口
 * 序列化的意义：将对象转化成字节序列，网络上数据的传输都是二进制的
 * 一个对象如果想被序列化，则这个对象的类需要实现Serializable接口（它的子接口也是可以的）
 * Serializable接口并没有定义任何方法，它的存在是告诉代码只有实现了该接口的类才可以被序列化，对程序来说是一种标记，真正的序列化工作并不是它完成的
 * serialVersionUID是序列化前后的唯一标识，建议在类中显式定义一个，当然如果没有显示定义的话，编译器也会隐式为你生成一个
 */
public interface RpcSerialization {

    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}
