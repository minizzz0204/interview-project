package com.shepherdmoney.interviewproject.exception;

public class UserEmptyEmailException extends RuntimeException{
    public UserEmptyEmailException(String message) {
        super(message);
    }
}
