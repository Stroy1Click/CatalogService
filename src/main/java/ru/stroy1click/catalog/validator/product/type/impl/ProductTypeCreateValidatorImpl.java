package ru.stroy1click.catalog.validator.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.exception.AlreadyExistsException;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.validator.product.type.ProductTypeCreateValidator;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeCreateValidatorImpl implements ProductTypeCreateValidator {

    private final ProductTypeService productTypeService;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductTypeDto productTypeDto){
        log.info("validate {}", productTypeDto);
        if(this.productTypeService.getByTitle(productTypeDto.getTitle()).isPresent()){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product_type.create.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
