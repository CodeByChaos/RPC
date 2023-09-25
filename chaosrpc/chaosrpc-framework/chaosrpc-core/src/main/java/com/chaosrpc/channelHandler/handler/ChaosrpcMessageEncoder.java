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
import java.nio.charset.StandardCharsets;

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
public class ChaosrpcMessageEncoder extends MessageToByteEncoder<ChaosrpcRequest> {
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
        // todo 不清楚总长度 writeIndex(写指针)
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        // 三个类型
        byteBuf.writeByte(chaosrpcRequest.getSerializeType());
        byteBuf.writeByte(chaosrpcRequest.getCompressType());
        byteBuf.writeByte(chaosrpcRequest.getRequestType());
        // 请求id
        byteBuf.writeLong(chaosrpcRequest.getRequestId());
        // 写入请求体（requestPlayload）
        byte[] body = getBodyBytes(chaosrpcRequest.getRequestPlayload());
        byteBuf.writeBytes(body);
        // 重新处理报文总长度
        // 先保存当前的写指针位置
        int writeIndex = byteBuf.writerIndex();
        // 将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(7);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + body.length);

        // 将写指针归位
        byteBuf.writeInt(writeIndex);
    }

    private byte[] getBodyBytes(RequestPlayload requestPlayload) {
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
