package com.chaosrpc.enumeration;

public enum ResponseCode {

    SUCCESS((byte) 1, "成功"),
    FAIL((byte) 2, "失败");

    private byte code;
    private String description;

    ResponseCode(byte code, String description) {
        this.code = code;
        this.description = description;
    }
}
