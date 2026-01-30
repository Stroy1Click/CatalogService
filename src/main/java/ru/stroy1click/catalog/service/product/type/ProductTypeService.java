package ru.stroy1click.catalog.service.product.type;


import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.service.CrudOperations;
import ru.stroy1click.catalog.service.ImageAssignmentService;

import java.util.Optional;

public interface ProductTypeService extends CrudOperations<Integer, ProductTypeDto>, ImageAssignmentService<Integer> {

    Optional<ProductType> getByTitle(String title);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String link);
}
