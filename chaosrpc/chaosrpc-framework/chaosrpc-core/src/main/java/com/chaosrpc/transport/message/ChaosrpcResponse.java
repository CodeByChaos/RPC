package com.chaosrpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChaosrpcResponse {

    // 请求id
    private long requestId;

    // 请求的类型，压缩的类型，序列化的方式
//    private byte requestType;
    private byte compressType;
    private byte serializeType;

    // 响应码 http 500以上 (int)
    // byte 节省空间
    // 1.成功 2.异常
    private byte code;

    // 具体的消息体
    private Object body;
}
