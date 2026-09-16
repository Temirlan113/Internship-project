package com.kz.internship_project.exception;

public class CustomFileNotFoundException extends RuntimeException {
    public CustomFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
