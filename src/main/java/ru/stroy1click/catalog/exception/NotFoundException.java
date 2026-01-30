package ru.stroy1click.catalog.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message){
        super(message);
    }
}
