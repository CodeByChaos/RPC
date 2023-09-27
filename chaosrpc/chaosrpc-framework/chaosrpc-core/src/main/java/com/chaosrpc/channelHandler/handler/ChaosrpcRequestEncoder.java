package com.chaosrpc.channelHandler.handler;

import com.chaosrpc.transport.message.ChaosrpcRequest;
import com.chaosrpc.transport.message.MessageFormatConstant;
import com.chaosrpc.transport.message.RequestPlayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 4B magic(魔数) ---> chaosrpc.getBytes()
 * 1B version(版本) ---> 1
 * 2B header length 首部的长度
 * 4B full length 报文总长度
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 * body
 * 出站时，第一个通过的处理器
 */
@Slf4j
public class ChaosrpcRequestEncoder extends MessageToByteEncoder<ChaosrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          ChaosrpcRequest chaosrpcRequest,
                          ByteBuf byteBuf)
            throws Exception {
        // 魔数
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度 writeIndex(写指针)
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 三个类型
        byteBuf.writeByte(chaosrpcRequest.getSerializeType());
        byteBuf.writeByte(chaosrpcRequest.getCompressType());
        byteBuf.writeByte(chaosrpcRequest.getRequestType());
        // 请求id
        byteBuf.writeLong(chaosrpcRequest.getRequestId());
//        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有playload
//        if(chaosrpcRequest.getRequestType() == RequestType.HEARTBEAT.getId()) {
//            // 处理一下总长度，总长度 = header长度
//            int writeIndex = byteBuf.writerIndex();
//            byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
//                    + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
//            byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH);
//            byteBuf.writerIndex(writeIndex);
//            return;
//        }
        // 写入请求体（requestPlayload）
        byte[] body = getBodyBytes(chaosrpcRequest.getRequestPlayload());
        if(body != null) {
            // 重新处理报文总长度
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;
        // 先保存当前的写指针位置
        int writeIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length
                + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);

        // 将写指针归位
        byteBuf.writerIndex(writeIndex);
        if(log.isDebugEnabled()) {
            log.debug("请求{}已经在调用端完成编码工作.", chaosrpcRequest.getRequestId());
        }
    }

    private byte[] getBodyBytes(RequestPlayload requestPlayload) {
        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有playload
        if(requestPlayload == null) {
            return null;
        }
        // 希望可以通过设计模式，面向对象的编程，让其可以配置修改序列化和压缩方式
        // 对象怎么变成一个字节数据 序列化 压缩
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(requestPlayload);

            // 压缩


           return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}
