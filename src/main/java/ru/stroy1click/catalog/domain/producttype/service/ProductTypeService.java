package ru.stroy1click.catalog.domain.producttype.service;


import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.domain.producttype.dto.ProductTypeDto;
import ru.stroy1click.catalog.domain.producttype.entity.ProductType;
import ru.stroy1click.catalog.domain.common.service.CrudOperations;
import ru.stroy1click.catalog.domain.common.service.ImageAssignmentService;

import java.util.Optional;

public interface ProductTypeService extends CrudOperations<Integer, ProductTypeDto>, ImageAssignmentService<Integer> {

    Optional<ProductType> getByTitle(String title);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String link);
}
