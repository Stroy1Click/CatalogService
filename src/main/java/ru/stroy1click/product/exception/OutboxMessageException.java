package ru.stroy1click.product.exception;

public class OutboxMessageException extends RuntimeException {
    public OutboxMessageException(String message) {
        super(message);
    }
}
