package ru.stroy1click.catalog.validator.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.entity.Product;
import ru.stroy1click.catalog.exception.AlreadyExistsException;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.validator.product.ProductUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductUpdateValidatorImpl implements ProductUpdateValidator {

    private final ProductService productService;

    private final MessageSource messageSource;

    @Override
    public void validate(ProductDto productDto){
        log.info("validate {}", productDto);
        Optional<Product> foundProduct = this.productService.getByTitle(productDto.getTitle());

        if(foundProduct.isPresent() && !Objects.equals(productDto.getId(), foundProduct.get().getId())
                && productDto.getTitle().equalsIgnoreCase(foundProduct.get().getTitle())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.product.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
