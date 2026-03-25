package ru.stroy1click.catalog.validator.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.entity.Product;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.validator.product.ProductUpdateValidator;
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
            throw ExceptionUtils.alreadyExists("error.product.update.validate", productDto.getTitle());
        }
    }
}
