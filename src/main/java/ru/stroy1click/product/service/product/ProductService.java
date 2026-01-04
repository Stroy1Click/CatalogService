package ru.stroy1click.product.service.product;

import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.entity.Product;
import ru.stroy1click.product.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface ProductService extends BaseService<Integer, ProductDto> {

    Optional<Product> getByTitle(String title);

    void assignImages(Integer id, List<MultipartFile> list);

    void deleteImage(Integer id, String link);
}
