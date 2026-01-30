package ru.stroy1click.catalog.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.ProductImageDto;
import ru.stroy1click.catalog.entity.ProductImage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductImageMapper implements Mappable<ProductImage, ProductImageDto>{

    private final ModelMapper modelMapper;

    @Override
    public ProductImage toEntity(ProductImageDto productImageDto) {
        return this.modelMapper.map(productImageDto, ProductImage.class);
    }

    @Override
    public ProductImageDto toDto(ProductImage productImage) {
        return this.modelMapper.map(productImage, ProductImageDto.class);
    }

    @Override
    public List<ProductImageDto> toDto(List<ProductImage> e) {
        return e.stream()
                .map(productImage -> this.modelMapper.map(productImage, ProductImageDto.class))
                .toList();
    }

    public List<ProductImage> toEntity(List<ProductImageDto> d){
        return d.stream()
                .map(productImage -> this.modelMapper.map(productImage, ProductImage.class))
                .toList();
    }
}
