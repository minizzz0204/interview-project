package com.shepherdmoney.interviewproject.exception;

public class UserEmptyNameException extends RuntimeException{
    public UserEmptyNameException(String message) {
        super(message);
    }
}
