package com.dosmartie;

public class OutOfQuantityException extends RuntimeException{
    public OutOfQuantityException(String message) {
        super(message);
    }
}
