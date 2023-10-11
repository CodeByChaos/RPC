package com.chaos.enumeration;

/**
 * 响应码需要做统一的处理
 * 成功码 20(方法成功调用) 21(心跳成功返回)
 * 负载码 31(服务器负载过高，被限流)
 * 错误码（客户端错误） 44(请求的资源未找到)
 * 错误码（服务端错误） 50(请求的方法不存在)
 * @author Chaos Wong
 */
public enum ResponseCode {

    SUCCESS((byte) 20, "成功"),
    SUCCESS_HEARTBEAT((byte) 21, "心跳检测成功返回"),
    RATE_LIMITER((byte) 31, "服务被限流"),
    RESOURCE_NOT_FOUND((byte) 44, "请求的资源不存在"),
    METHOD_NOT_FOUND((byte) 50, "调用方法发生异常"),
    FAIL((byte) 2, "失败"),
    CLOSING((byte) 51,"正在关闭中");

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
