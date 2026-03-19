package com.example.userService.exception;

public class DuplicateUserException extends RuntimeException{
    public DuplicateUserException(String message){
        super(message);
    }
}
