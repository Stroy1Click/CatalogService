package ru.stroy1click.catalog.validator.subcategory.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.exception.AlreadyExistsException;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;
import ru.stroy1click.catalog.validator.subcategory.SubcategoryUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubcategoryUpdateValidatorImpl implements SubcategoryUpdateValidator {

    private final SubcategoryService subcategoryService;

    private final MessageSource messageSource;

    @Override
    public void validate(SubcategoryDto subcategoryDto){
        log.info("validate {}", subcategoryDto);
        Optional<Subcategory> foundSubcategory= this.subcategoryService.getByTitle(subcategoryDto.getTitle());

        if(foundSubcategory.isPresent() && !Objects.equals(subcategoryDto.getId(), foundSubcategory.get().getId())
                && subcategoryDto.getTitle().equalsIgnoreCase(foundSubcategory.get().getTitle())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.subcategory.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
