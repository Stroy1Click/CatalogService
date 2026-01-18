package ru.stroy1click.product.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.model.PageResponse;
import ru.stroy1click.product.model.ProductAttributeFilter;

import java.util.List;

public interface ProductPaginationService {

    PageResponse<ProductDto> getProducts(Integer categoryId,
                                         Integer subcategoryId,
                                         Integer productType,
                                         Pageable pageable);
}
