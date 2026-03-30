package ru.stroy1click.catalog.domain.subcategory.service;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.domain.producttype.dto.ProductTypeDto;
import ru.stroy1click.catalog.domain.subcategory.dto.SubcategoryDto;
import ru.stroy1click.catalog.domain.subcategory.entity.Subcategory;
import ru.stroy1click.catalog.domain.common.service.CrudOperations;
import ru.stroy1click.catalog.domain.common.service.ImageAssignmentService;

import java.util.List;
import java.util.Optional;

public interface SubcategoryService extends CrudOperations<Integer, SubcategoryDto>, ImageAssignmentService<Integer> {

    Optional<Subcategory> getByTitle(String title);

    List<ProductTypeDto> getProductTypes(Integer id);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String imageName);
}
