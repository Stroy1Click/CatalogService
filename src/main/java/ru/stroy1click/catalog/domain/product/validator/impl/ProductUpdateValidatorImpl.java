package ru.stroy1click.catalog.domain.product.validator.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.catalog.domain.product.entity.Product;
import ru.stroy1click.catalog.domain.product.service.ProductService;
import ru.stroy1click.catalog.domain.product.validator.ProductUpdateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdateValidatorImpl implements ProductUpdateValidator {

    private final ProductService productService;

    @Override
    public void validate(ProductDto productDto){
        log.info("validate {}", productDto);
        Optional<Product> foundProduct = this.productService.getByTitle(productDto.getTitle());

        if(foundProduct.isPresent() && !Objects.equals(productDto.getId(), foundProduct.get().getId())
                && productDto.getTitle().equalsIgnoreCase(foundProduct.get().getTitle())){
            throw ExceptionUtils.alreadyExists("error.product.update.validate", null);
        }
    }
}
