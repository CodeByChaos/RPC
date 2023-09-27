package com.chaosrpc.channelHandler.handler;

import com.chaosrpc.enumeration.RequestType;
import com.chaosrpc.transport.message.ChaosrpcRequest;
import com.chaosrpc.transport.message.MessageFormatConstant;
import com.chaosrpc.transport.message.RequestPlayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * 基于长度字段的帧解码器
 */
@Slf4j
public class ChaosrpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public ChaosrpcRequestDecoder() {
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个 maxFrameLength 值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量
                MessageFormatConstant.MAGIC.length
                        + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // todo 负载的适配长度
                -(MessageFormatConstant.MAGIC.length
                        + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH
                        + MessageFormatConstant.FULL_FIELD_LENGTH),
                0
        );

    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 1.解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        // 检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if(magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("获得的请求类型不合法。");
            }
        }

        // 2.解析版本号
        byte version = byteBuf.readByte();
        if(version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得的请求版本不被支持。");
        }

        // 3.解析头部的长度
        short headLength = byteBuf.readShort();

        // 4.解析总长度
        int fullLength = byteBuf.readInt();

        // 5.请求类型 判断是不是心跳检测
        byte requestType = byteBuf.readByte();
        // 6.序列化类型
        byte serializeType = byteBuf.readByte();
        // 7.压缩类型
        byte compressType = byteBuf.readByte();
        // 8.请求id
        long requestId = byteBuf.readLong();

        // 我们需要封装
        ChaosrpcRequest chaosrpcRequest = new ChaosrpcRequest();
        chaosrpcRequest.setRequestId(requestId);
        chaosrpcRequest.setCompressType(compressType);
        chaosrpcRequest.setSerializeType(serializeType);
        chaosrpcRequest.setRequestType(requestType);

        // 心跳请求没有负载，此处可以判断并直接返回
        if (requestType == RequestType.HEARTBEAT.getId()) {
            return chaosrpcRequest;
        }

        int playloadLength = fullLength - headLength;
        byte[] playload = new byte[playloadLength];
        byteBuf.readBytes(playload);

        // 有了字节数组之后就可以解压缩，反序列化
        // todo 解压

        // todo 反序列化


        try (ByteArrayInputStream bis = new ByteArrayInputStream(playload);
             ObjectInputStream ois = new ObjectInputStream(bis)){
            RequestPlayload requestPlayload = (RequestPlayload) ois.readObject();
            chaosrpcRequest.setRequestPlayload(requestPlayload);
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求{}反序列化时发生了异常。", requestId, e);
            throw new RuntimeException(e);
        }
        if(log.isDebugEnabled()) {
            log.debug("请求{}已经在服务端完成解码工作.", chaosrpcRequest.getRequestId());
        }
        return chaosrpcRequest;
    }
}
