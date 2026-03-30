package ru.stroy1click.catalog.domain.product.image.service;

import ru.stroy1click.catalog.domain.product.image.dto.ProductImageDto;

import java.util.List;

public interface ProductImageService {

    List<ProductImageDto> getAllByProductId(Integer productId);

    void create(List<ProductImageDto> list);

    void create(ProductImageDto productImageDto);

    void update(Integer id, ProductImageDto productImageDto);

    void delete(String link);
}
