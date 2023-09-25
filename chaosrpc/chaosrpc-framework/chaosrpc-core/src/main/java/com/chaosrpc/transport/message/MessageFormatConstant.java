package com.chaosrpc.transport.message;

public class MessageFormatConstant {
    // todo 此处数据可能需要更改
    public static final byte[] MAGIC = "srpc".getBytes();
    public static final byte VERSION = 1;
    public static final short HEADER_LENGTH = (byte)(MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    public static final short FULL_LENGTH = 4;


}
