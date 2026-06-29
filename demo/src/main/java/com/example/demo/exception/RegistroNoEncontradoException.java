package com.example.demo.exception;

public class RegistroNoEncontradoException extends RuntimeException {
    public RegistroNoEncontradoException(String message) {
        super(message);
    }
}
