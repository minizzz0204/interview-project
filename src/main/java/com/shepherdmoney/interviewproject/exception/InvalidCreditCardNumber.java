package com.shepherdmoney.interviewproject.exception;

public class InvalidCreditCardNumber extends RuntimeException{
    public InvalidCreditCardNumber(String message) {
        super(message);
    }
}
