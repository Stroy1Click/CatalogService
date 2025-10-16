package ru.stroy1click.product.validator.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.ProductAttributeValueDto;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.product.ProductAttributeValueService;
import ru.stroy1click.product.validator.base.CreateValidator;
import ru.stroy1click.product.validator.product.ProductAttributeValueCreateValidator;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAttributeValueCreateValidatorImpl implements ProductAttributeValueCreateValidator {

    private final ProductAttributeValueService service;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductAttributeValueDto dto) {
        log.info("validate {}", dto);
        if(this.service.getByValue(dto.getValue()).isPresent()){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product_value_attribute.create.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
