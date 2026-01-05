package ru.stroy1click.product.service.subcategory;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.ProductTypeDto;
import ru.stroy1click.product.dto.SubcategoryDto;
import ru.stroy1click.product.entity.Subcategory;
import ru.stroy1click.product.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface SubcategoryService extends BaseService<Integer, SubcategoryDto> {

    Optional<Subcategory> getByTitle(String title);

    List<ProductTypeDto> getProductTypes(Integer id);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String imageName);
}
