package ru.stroy1click.catalog.service.category;

import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.service.CrudOperations;
import ru.stroy1click.catalog.service.ImageAssignmentService;

import java.util.List;
import java.util.Optional;

public interface CategoryService extends CrudOperations<Integer, CategoryDto>, ImageAssignmentService<Integer> {
    Optional<Category> getByTitle(String title);

    List<SubcategoryDto> getSubcategories(Integer id);
}
