package ru.stroy1click.catalog.validator.category.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.validator.category.CategoryUpdateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryUpdateValidatorImpl implements CategoryUpdateValidator {

    private final CategoryService categoryService;

    @Override
    public void validate(CategoryDto categoryDto){
        log.info("validate {}", categoryDto);
        Optional<Category> foundCategory = this.categoryService.getByTitle(categoryDto.getTitle());

        if(foundCategory.isPresent() && !Objects.equals(categoryDto.getId(), foundCategory.get().getId())
                && categoryDto.getTitle().equalsIgnoreCase(foundCategory.get().getTitle())){
            throw ExceptionUtils.alreadyExists("error.category.update.validate", categoryDto.getTitle());
        }
    }
}
