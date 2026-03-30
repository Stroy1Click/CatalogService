package ru.stroy1click.catalog.domain.subcategory.validator.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.domain.subcategory.dto.SubcategoryDto;
import ru.stroy1click.catalog.domain.subcategory.service.SubcategoryService;
import ru.stroy1click.catalog.domain.subcategory.validator.SubcategoryCreateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubcategoryCreateValidatorImpl implements SubcategoryCreateValidator {

    private final SubcategoryService subcategoryService;

    @Override
    public void validate(SubcategoryDto subcategoryDto){
        log.info("validate {}", subcategoryDto);
        if(this.subcategoryService.getByTitle(subcategoryDto.getTitle()).isPresent()){
            throw ExceptionUtils.alreadyExists("error.subcategory.create.validate", null);
        }
    }
}
