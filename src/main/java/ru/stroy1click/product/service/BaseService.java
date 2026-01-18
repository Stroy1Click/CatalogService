package ru.stroy1click.product.service;

import ru.stroy1click.product.dto.ProductDto;

public interface BaseService<ID, T> {

    T get(ID id);

    T create(T dto);

    void update(ID id, T dto);

    void delete(ID id);
}
