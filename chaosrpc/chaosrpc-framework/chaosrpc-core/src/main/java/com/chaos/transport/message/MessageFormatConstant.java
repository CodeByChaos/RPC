package com.chaos.transport.message;

public class MessageFormatConstant {
    // todo 此处数据可能需要更改
    public static final byte[] MAGIC = "chaosrpc".getBytes();
    public static final byte VERSION = 1;

    // 头部信息的长度
    public static final short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8 + 8);
    // 头部信息的长度占用的字节数
    public static final int HEADER_FIELD_LENGTH = 2;

    public static final short FULL_LENGTH = 4;

    public static final int MAX_FRAME_LENGTH = 1024 * 1024;

    public static final int VERSION_LENGTH = 1;

    // 总长度占用的字节数
    public static final int FULL_FIELD_LENGTH = 4;
}
