package com.chaos.exceptions;

/**
 * @author WongYut
 */
public class CompressException extends RuntimeException{
    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }

    public CompressException(Throwable cause) {
        super(cause);
    }
}
