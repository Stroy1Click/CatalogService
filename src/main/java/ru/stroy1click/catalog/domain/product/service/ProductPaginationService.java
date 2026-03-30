package ru.stroy1click.catalog.domain.product.service;

import org.springframework.data.domain.Pageable;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.catalog.domain.common.dto.PageResponse;

public interface ProductPaginationService {

    PageResponse<ProductDto> getProducts(Integer categoryId,
                                         Integer subcategoryId,
                                         Integer productType,
                                         Pageable pageable);
}
