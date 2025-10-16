package ru.stroy1click.product.validator.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.ProductTypeDto;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.product.type.ProductTypeService;
import ru.stroy1click.product.validator.product.type.ProductTypeCreateValidator;

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
