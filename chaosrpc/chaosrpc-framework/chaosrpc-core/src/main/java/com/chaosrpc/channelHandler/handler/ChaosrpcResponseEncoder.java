package com.chaosrpc.channelHandler.handler;

import com.chaosrpc.transport.message.ChaosrpcResponse;
import com.chaosrpc.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class ChaosrpcResponseEncoder extends MessageToByteEncoder<ChaosrpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext,
                          ChaosrpcResponse chaosrpcResponse,
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
        byteBuf.writeByte(chaosrpcResponse.getSerializeType());
        byteBuf.writeByte(chaosrpcResponse.getCompressType());
        byteBuf.writeByte(chaosrpcResponse.getCode());
        // 请求id
        byteBuf.writeLong(chaosrpcResponse.getRequestId());
//        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有playload "ping" "pong"
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
        byte[] body = getBodyBytes(chaosrpcResponse.getBody());
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
            log.debug("响应{}已经在服务端完成编码工作.", chaosrpcResponse.getRequestId());
        }
    }

    private byte[] getBodyBytes(Object body) {
        // 针对不同的消息类型需要做不同的处理，心跳的请求，没有playload
        if(body == null) {
            return null;
        }
        // 希望可以通过设计模式，面向对象的编程，让其可以配置修改序列化和压缩方式
        // 对象怎么变成一个字节数据 序列化 压缩
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(body);

            // 压缩


            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}
