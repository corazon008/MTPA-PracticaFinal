package org.example.exception;

/**
 * Base exception for protocol-related errors.
 */
public class ProtocolException extends Exception {
    private String errorCode;

    public ProtocolException(String message) {
        super(message);
        this.errorCode = "SERVER_ERROR";
    }

    public ProtocolException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

