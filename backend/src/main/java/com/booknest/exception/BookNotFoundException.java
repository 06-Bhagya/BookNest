package com.booknest.exception;

public class BookNotFoundException extends RuntimeException {   //validation and Exception handling

    public BookNotFoundException(String message) {
        super(message);
    }
}