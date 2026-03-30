package ru.stroy1click.catalog.domain.subcategory.validator.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.domain.subcategory.dto.SubcategoryDto;
import ru.stroy1click.catalog.domain.subcategory.entity.Subcategory;
import ru.stroy1click.catalog.domain.subcategory.service.SubcategoryService;
import ru.stroy1click.catalog.domain.subcategory.validator.SubcategoryUpdateValidator;
import ru.stroy1click.common.util.ExceptionUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubcategoryUpdateValidatorImpl implements SubcategoryUpdateValidator {

    private final SubcategoryService subcategoryService;

    @Override
    public void validate(SubcategoryDto subcategoryDto){
        log.info("validate {}", subcategoryDto);
        Optional<Subcategory> foundSubcategory= this.subcategoryService.getByTitle(subcategoryDto.getTitle());

        if(foundSubcategory.isPresent() && !Objects.equals(subcategoryDto.getId(), foundSubcategory.get().getId())
                && subcategoryDto.getTitle().equalsIgnoreCase(foundSubcategory.get().getTitle())){
            throw ExceptionUtils.alreadyExists("error.subcategory.update.validate", null);
        }
    }
}
