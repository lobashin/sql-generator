package org.example.app.exception;

public class UnsupportedSqlCommandException extends RuntimeException {
    public UnsupportedSqlCommandException(String message) {
        super(message);
    }
}
