package ru.stroy1click.product.service.category;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.dto.SubcategoryDto;
import ru.stroy1click.product.entity.Category;
import ru.stroy1click.product.service.CrudOperations;
import ru.stroy1click.product.service.ImageAssignmentService;

import java.util.List;
import java.util.Optional;

public interface CategoryService extends CrudOperations<Integer, CategoryDto>, ImageAssignmentService<Integer> {
    Optional<Category> getByTitle(String title);

    List<SubcategoryDto> getSubcategories(Integer id);
}
