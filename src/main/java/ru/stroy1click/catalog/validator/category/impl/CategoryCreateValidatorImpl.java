package ru.stroy1click.catalog.validator.category.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.validator.category.CategoryCreateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryCreateValidatorImpl implements CategoryCreateValidator {

    private final CategoryService categoryService;

    @Override
    public void validate(CategoryDto categoryDto){
        log.info("validate {}", categoryDto);
        if(this.categoryService.getByTitle(categoryDto.getTitle()).isPresent()) {
            throw ExceptionUtils.alreadyExists("error.category.create.validate", null);
        }
    }
}