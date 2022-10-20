package com.harbor.calendly.exceptions;

public class InvalidEventException extends Exception {
    public InvalidEventException(String message) {
        super(message);
    }
}
