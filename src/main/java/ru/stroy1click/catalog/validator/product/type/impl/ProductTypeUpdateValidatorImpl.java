package ru.stroy1click.catalog.validator.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.exception.AlreadyExistsException;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.validator.product.type.ProductTypeUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductTypeUpdateValidatorImpl implements ProductTypeUpdateValidator {

    private final ProductTypeService productTypeService;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductTypeDto productTypeDto){
        log.info("validate {}", productTypeDto);
        Optional<ProductType> foundSubcategory= this.productTypeService.getByTitle(productTypeDto.getTitle());

        if(foundSubcategory.isPresent() && !Objects.equals(productTypeDto.getId(), foundSubcategory.get().getId())
                && productTypeDto.getTitle().equalsIgnoreCase(foundSubcategory.get().getTitle())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product_type.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}