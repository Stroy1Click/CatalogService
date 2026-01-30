package ru.stroy1click.catalog.validator.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.exception.AlreadyExistsException;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.validator.product.ProductCreateValidator;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCreateValidatorImpl implements ProductCreateValidator {

    private final ProductService productService;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductDto productDto){
        log.info("validate {}", productDto);
        if(this.productService.getByTitle(productDto.getTitle()).isPresent()){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product.create.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
