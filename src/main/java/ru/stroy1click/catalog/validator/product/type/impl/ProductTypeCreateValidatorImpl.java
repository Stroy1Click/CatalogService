package ru.stroy1click.catalog.validator.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.validator.product.type.ProductTypeCreateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeCreateValidatorImpl implements ProductTypeCreateValidator {

    private final ProductTypeService productTypeService;

    @Override
    public void validate(ProductTypeDto productTypeDto){
        log.info("validate {}", productTypeDto);
        if(this.productTypeService.getByTitle(productTypeDto.getTitle()).isPresent()){
            throw ExceptionUtils.alreadyExists("error.product_type.create.validate", productTypeDto.getTitle());
        }
    }
}
