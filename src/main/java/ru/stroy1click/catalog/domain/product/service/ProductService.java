package ru.stroy1click.catalog.domain.product.service;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.catalog.domain.product.entity.Product;
import ru.stroy1click.catalog.domain.common.service.CrudOperations;

import java.util.List;
import java.util.Optional;

public interface ProductService extends CrudOperations<Integer, ProductDto> {

    Optional<Product> getByTitle(String title);

    void assignImages(Integer id, List<MultipartFile> list);

    void deleteImage(Integer id, String link);
}
