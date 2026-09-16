package com.kz.internship_project.exception;

public class CustomFileStorageException extends RuntimeException{

    public CustomFileStorageException(String message, Throwable cause){
        super(message, cause);
    }
}
