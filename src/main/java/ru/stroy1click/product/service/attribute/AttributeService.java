package ru.stroy1click.product.service.attribute;

import ru.stroy1click.product.dto.AttributeDto;
import ru.stroy1click.product.entity.Attribute;
import ru.stroy1click.product.service.BaseService;

import java.util.Optional;

public interface AttributeService extends BaseService<Integer, AttributeDto> {

    Optional<Attribute> getByTitle(String title);
}
