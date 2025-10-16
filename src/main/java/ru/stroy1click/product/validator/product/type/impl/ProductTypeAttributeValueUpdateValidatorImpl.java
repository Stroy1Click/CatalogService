package ru.stroy1click.product.validator.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.ProductTypeAttributeValueDto;
import ru.stroy1click.product.entity.ProductType;
import ru.stroy1click.product.entity.ProductTypeAttributeValue;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.product.type.ProductTypeAttributeValueService;
import ru.stroy1click.product.validator.product.type.ProductTypeAttributeValueUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeAttributeValueUpdateValidatorImpl implements ProductTypeAttributeValueUpdateValidator {

    private final ProductTypeAttributeValueService service;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductTypeAttributeValueDto dto) {
        log.info("validate {}", dto);
        Optional<ProductTypeAttributeValue> foundProductTypeAttributeValue = this.service.getByValue(dto.getValue());

        if(foundProductTypeAttributeValue.isPresent() && !Objects.equals(dto.getId(), foundProductTypeAttributeValue.get().getId())
                && dto.getValue().equalsIgnoreCase(foundProductTypeAttributeValue.get().getValue())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product_type_value_attribute.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
