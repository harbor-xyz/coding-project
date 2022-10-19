package com.harbor.calendly.exceptions;

public class InvalidEventBookException extends Exception {
    public InvalidEventBookException(String message) {
        super(message);
    }
}
