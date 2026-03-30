package ru.stroy1click.catalog.domain.product.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.catalog.domain.product.entity.Product;
import ru.stroy1click.common.mapper.Mappable;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper implements Mappable<Product, ProductDto>{

    private final ModelMapper modelMapper;

    @Override
    public Product toEntity(ProductDto productDto) {
        return this.modelMapper.map(productDto, Product.class);
    }

    @Override
    public ProductDto toDto(Product product) {
        return this.modelMapper.map(product, ProductDto.class);
    }

    @Override
    public List<ProductDto> toDto(List<Product> e) {
        return e.stream()
                .map(product -> this.modelMapper.map(product, ProductDto.class))
                .toList();
    }
}
