package ru.stroy1click.catalog.service.subcategory;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.service.CrudOperations;
import ru.stroy1click.catalog.service.ImageAssignmentService;

import java.util.List;
import java.util.Optional;

public interface SubcategoryService extends CrudOperations<Integer, SubcategoryDto>, ImageAssignmentService<Integer> {

    Optional<Subcategory> getByTitle(String title);

    List<ProductTypeDto> getProductTypes(Integer id);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String imageName);
}
