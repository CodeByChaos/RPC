package com.chaos.channelHandler.handler;

import com.chaos.compress.CompressFactory;
import com.chaos.compress.Compressor;
import com.chaos.serialize.Serializer;
import com.chaos.serialize.SerializerFactory;
import com.chaos.transport.message.ChaosrpcResponse;
import com.chaos.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

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
        byteBuf.writeLong(chaosrpcResponse.getTimeStamp());
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
        // 对响应做序列化
        byte[] body = null;
        if(chaosrpcResponse.getBody() != null) {
            Serializer serializer = SerializerFactory
                    .getSerializer(chaosrpcResponse.getSerializeType())
                    .getSerializer();
            body = serializer.serialize(chaosrpcResponse.getBody());
            // 压缩
            Compressor compressor = CompressFactory
                    .getCompress(chaosrpcResponse.getCompressType())
                    .getCompressor();
            body = compressor.compress(body);

        }
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

}
