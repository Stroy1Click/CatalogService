package ru.stroy1click.product.validator.base;

import ru.stroy1click.product.dto.*;

public interface UpdateValidator<T> {

    void validate(T dto);
}
