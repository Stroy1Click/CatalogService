package ru.stroy1click.product.validator.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.ProductAttributeValueDto;
import ru.stroy1click.product.entity.Product;
import ru.stroy1click.product.entity.ProductAttributeValue;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.product.ProductAttributeValueService;
import ru.stroy1click.product.validator.base.CreateValidator;
import ru.stroy1click.product.validator.base.UpdateValidator;
import ru.stroy1click.product.validator.product.ProductAttributeValueUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductAttributeValueUpdateValidatorImpl implements ProductAttributeValueUpdateValidator {

    private final ProductAttributeValueService service;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductAttributeValueDto dto) {
        log.info("validate {}", dto);
        Optional<ProductAttributeValue> foundProductAttributeValue = this.service.getByValue(dto.getValue());

        if(foundProductAttributeValue.isPresent() && !Objects.equals(dto.getId(), foundProductAttributeValue.get().getId())
                && dto.getValue().equalsIgnoreCase(foundProductAttributeValue.get().getValue())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product_value_attribute.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
