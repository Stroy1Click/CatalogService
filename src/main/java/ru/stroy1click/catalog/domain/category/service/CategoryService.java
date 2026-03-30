package ru.stroy1click.catalog.domain.category.service;

import ru.stroy1click.catalog.domain.category.dto.CategoryDto;
import ru.stroy1click.catalog.domain.subcategory.dto.SubcategoryDto;
import ru.stroy1click.catalog.domain.category.entity.Category;
import ru.stroy1click.catalog.domain.common.service.CrudOperations;
import ru.stroy1click.catalog.domain.common.service.ImageAssignmentService;

import java.util.List;
import java.util.Optional;

public interface CategoryService extends CrudOperations<Integer, CategoryDto>, ImageAssignmentService<Integer> {
    Optional<Category> getByTitle(String title);

    List<SubcategoryDto> getSubcategories(Integer id);
}
