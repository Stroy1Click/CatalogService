package ru.stroy1click.product.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException() {

    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
