package ru.stroy1click.product.service.product.type;

import ru.stroy1click.product.dto.ProductTypeAttributeValueDto;
import ru.stroy1click.product.entity.ProductTypeAttributeValue;
import ru.stroy1click.product.service.BaseService;

import java.util.List;
import java.util.Optional;

public interface ProductTypeAttributeValueService extends BaseService<Integer, ProductTypeAttributeValueDto> {

    List<ProductTypeAttributeValueDto> getAllByProductId(Integer id);

    Optional<ProductTypeAttributeValue> getByValue(String value);
}
