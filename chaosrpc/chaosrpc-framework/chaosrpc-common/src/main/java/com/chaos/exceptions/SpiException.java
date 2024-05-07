package com.chaos.exceptions;

/**
 * @author WongYut
 */
public class SpiException extends RuntimeException{
    public SpiException() {
    }

    public SpiException(String message) {
        super(message);
    }

    public SpiException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpiException(Throwable cause) {
        super(cause);
    }

    public SpiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
