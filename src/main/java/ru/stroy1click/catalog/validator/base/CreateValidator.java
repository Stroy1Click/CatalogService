package ru.stroy1click.catalog.validator.base;

public interface CreateValidator<T> {

    void validate(T dto);
}
