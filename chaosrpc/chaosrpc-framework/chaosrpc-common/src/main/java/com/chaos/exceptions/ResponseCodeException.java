package com.chaos.exceptions;

/**
 * @author Chaos Wong
 */
public class ResponseCodeException extends RuntimeException{
    private final byte code;
    private final String message;

    public ResponseCodeException(byte code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseCodeException(String message, byte code) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ResponseCodeException(byte code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public ResponseCodeException(Throwable cause, byte code, String message) {
        super(cause);
        this.code = code;
        this.message = message;
    }

    public ResponseCodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, byte code, String message1) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.message = message1;
    }
}
