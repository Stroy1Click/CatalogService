package ru.stroy1click.product.service.product.type;


import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.ProductTypeDto;
import ru.stroy1click.product.entity.ProductType;
import ru.stroy1click.product.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface ProductTypeService extends BaseService<Integer, ProductTypeDto> {

    Optional<ProductType> getByTitle(String title);

    void assignImage(Integer id, MultipartFile image);

    void deleteImage(Integer id, String link);
}
