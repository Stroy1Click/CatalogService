package ru.stroy1click.product.validator.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.ProductTypeAttributeValueDto;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.product.type.ProductTypeAttributeValueService;
import ru.stroy1click.product.validator.product.type.ProductTypeAttributeValueCreateValidator;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeAttributeValueCreateValidatorImpl implements ProductTypeAttributeValueCreateValidator {

    private final ProductTypeAttributeValueService service;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductTypeAttributeValueDto dto) {
        log.info("validate {}", dto);
        if(this.service.getByValue(dto.getValue()).isPresent()){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product_type_value_attribute.create.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
