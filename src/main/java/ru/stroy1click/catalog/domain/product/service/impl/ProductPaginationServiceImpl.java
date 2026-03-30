package ru.stroy1click.catalog.domain.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.catalog.domain.common.dto.PageResponse;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.catalog.domain.product.repository.ProductRepository;
import ru.stroy1click.catalog.domain.product.service.ProductPaginationService;
import ru.stroy1click.catalog.domain.product.service.ProductService;
import ru.stroy1click.common.exception.ValidationException;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductPaginationServiceImpl implements ProductPaginationService {

    private final ProductRepository productRepository;

    private final ProductService productService;

    private final MessageSource messageSource;

    @Override
    public PageResponse<ProductDto> getProducts(Integer categoryId,
                                                Integer subcategoryId,
                                                Integer productTypeId,
                                                Pageable pageable){
        Page<Integer> productIds;

        if (categoryId != null) {
            productIds = this.productRepository.findProductIdsByCategory_Id(categoryId, pageable);
        } else if (subcategoryId != null) {
            productIds = this.productRepository.findProductIdsBySubcategory_Id(subcategoryId, pageable);
        } else if (productTypeId != null) {
            productIds = this.productRepository.findProductIdsByProductType_Id(productTypeId, pageable);
        } else {
            throw new ValidationException(
                    this.messageSource.getMessage(
                            "error.filter.empty",
                            null,
                            Locale.getDefault()
                    )
            );
        }

        List<ProductDto> products = productIds.getContent().stream()
                .map(this.productService::get)
                .toList();

        return new PageResponse<>(
                products, pageable.getPageNumber(), pageable.getPageSize(),
                productIds.getTotalElements(), productIds.getTotalPages(),
                productIds.isLast()
        );
    }
}