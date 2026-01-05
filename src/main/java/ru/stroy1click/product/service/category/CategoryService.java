package ru.stroy1click.product.service.category;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.dto.SubcategoryDto;
import ru.stroy1click.product.entity.Category;
import ru.stroy1click.product.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface CategoryService extends BaseService<Integer, CategoryDto> {

    List<CategoryDto> getAll();

    Optional<Category> getByTitle(String title);

    List<SubcategoryDto> getSubcategories(Integer id);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String imageName);
}
