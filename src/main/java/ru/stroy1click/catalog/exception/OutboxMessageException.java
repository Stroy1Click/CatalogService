package ru.stroy1click.catalog.exception;

public class OutboxMessageException extends RuntimeException {
    public OutboxMessageException(String message) {
        super(message);
    }
}
