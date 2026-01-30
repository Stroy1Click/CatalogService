package ru.stroy1click.catalog.validator.subcategory.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.exception.AlreadyExistsException;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;
import ru.stroy1click.catalog.validator.subcategory.SubcategoryCreateValidator;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubcategoryCreateValidatorImpl implements SubcategoryCreateValidator {

    private final SubcategoryService subcategoryService;

    private final MessageSource messageSource;

    @Override
    public void validate(SubcategoryDto subcategoryDto){
        log.info("validate {}", subcategoryDto);
        if(this.subcategoryService.getByTitle(subcategoryDto.getTitle()).isPresent()){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.subcategory.create.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
