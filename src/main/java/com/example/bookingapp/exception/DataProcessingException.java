package com.example.bookingapp.exception;

public class DataProcessingException extends RuntimeException {
    public DataProcessingException(final String message) {
        super(message);
    }
}
