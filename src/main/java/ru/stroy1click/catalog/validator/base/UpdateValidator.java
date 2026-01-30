package ru.stroy1click.catalog.validator.base;

public interface UpdateValidator<T> {

    void validate(T dto);
}
