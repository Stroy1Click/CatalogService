package ru.stroy1click.product.validator.category.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.entity.Category;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.category.CategoryService;
import ru.stroy1click.product.validator.base.UpdateValidator;
import ru.stroy1click.product.validator.category.CategoryUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryUpdateValidatorImpl implements CategoryUpdateValidator {

    private final CategoryService categoryService;

    private final MessageSource messageSource;

    @Override
    public void validate(CategoryDto categoryDto){
        log.info("validate {}", categoryDto);
        Optional<Category> foundCategory = this.categoryService.getByTitle(categoryDto.getTitle());

        if(foundCategory.isPresent() && !Objects.equals(categoryDto.getId(), foundCategory.get().getId())
                && categoryDto.getTitle().equalsIgnoreCase(foundCategory.get().getTitle())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.category.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
