package ru.stroy1click.product.exception;

public class ServiceErrorResponseException extends RuntimeException {

    public ServiceErrorResponseException(String message) {
        super(message);
    }
}
