package ru.stroy1click.product.service;

public interface BaseService<ID, T> {

    T get(ID id);

    void create(T dto);

    void update(ID id, T dto);

    void delete(ID id);
}
