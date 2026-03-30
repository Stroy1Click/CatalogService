package ru.stroy1click.catalog.domain.producttype.validator.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.domain.producttype.dto.ProductTypeDto;
import ru.stroy1click.catalog.domain.producttype.entity.ProductType;
import ru.stroy1click.catalog.domain.producttype.service.ProductTypeService;
import ru.stroy1click.catalog.domain.producttype.validator.ProductTypeUpdateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeUpdateValidatorImpl implements ProductTypeUpdateValidator {

    private final ProductTypeService productTypeService;

    @Override
    public void validate(ProductTypeDto productTypeDto){
        log.info("validate {}", productTypeDto);
        Optional<ProductType> foundSubcategory= this.productTypeService.getByTitle(productTypeDto.getTitle());

        if(foundSubcategory.isPresent() && !Objects.equals(productTypeDto.getId(), foundSubcategory.get().getId())
                && productTypeDto.getTitle().equalsIgnoreCase(foundSubcategory.get().getTitle())){
            throw ExceptionUtils.alreadyExists("error.product_type.update.validate", null);
        }
    }
}