package ru.stroy1click.product.mapper;

import java.util.List;

public interface Mappable<E, D>{

    E toEntity(D d);

    D toDto(E e);

    List<D> toDto(List<E> e);
}
