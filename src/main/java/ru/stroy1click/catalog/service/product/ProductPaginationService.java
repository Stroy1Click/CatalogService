package ru.stroy1click.catalog.service.product;

import org.springframework.data.domain.Pageable;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.dto.PageResponse;

public interface ProductPaginationService {

    PageResponse<ProductDto> getProducts(Integer categoryId,
                                         Integer subcategoryId,
                                         Integer productType,
                                         Pageable pageable);
}
