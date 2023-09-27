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

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
