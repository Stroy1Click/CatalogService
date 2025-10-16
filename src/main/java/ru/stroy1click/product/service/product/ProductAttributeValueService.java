package ru.stroy1click.product.service.product;

import ru.stroy1click.product.dto.ProductAttributeValueDto;
import ru.stroy1click.product.entity.ProductAttributeValue;
import ru.stroy1click.product.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface ProductAttributeValueService extends BaseService<Integer, ProductAttributeValueDto> {

    List<ProductAttributeValueDto> getAllByProductId(Integer id);

    Optional<ProductAttributeValue> getByValue(String value);
}
