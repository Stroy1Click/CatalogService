package ru.stroy1click.catalog.exception;

public class ServiceErrorResponseException extends RuntimeException {

    public ServiceErrorResponseException(String message) {
        super(message);
    }
}
