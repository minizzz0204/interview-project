package com.shepherdmoney.interviewproject.exception;

public class InvalidUserIdException extends RuntimeException{
    public InvalidUserIdException(String message) {
        super(message);
    }
}
