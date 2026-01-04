package ru.stroy1click.product.service.product;

import org.springframework.data.domain.Pageable;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.model.ProductAttributeFilter;

import java.util.List;

public interface ProductPaginationService {

    List<ProductDto> getProducts(Integer categoryId,
                                 Integer subcategoryId,
                                 Integer productType,
                                 Pageable pageable);

    List<ProductDto> getByFilter(ProductAttributeFilter productAttributeFilter, Pageable pageable);
}
