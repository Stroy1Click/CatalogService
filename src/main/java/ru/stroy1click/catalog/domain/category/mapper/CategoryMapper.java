package ru.stroy1click.catalog.domain.category.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.domain.category.dto.CategoryDto;
import ru.stroy1click.catalog.domain.category.entity.Category;
import ru.stroy1click.common.mapper.Mappable;


import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryMapper implements Mappable<Category, CategoryDto> {

    private final ModelMapper modelMapper;

    @Override
    public Category toEntity(CategoryDto categoryDto) {
        return this.modelMapper.map(categoryDto, Category.class);
    }

    @Override
    public CategoryDto toDto(Category category) {
        return this.modelMapper.map(category, CategoryDto.class);
    }

    @Override
    public List<CategoryDto> toDto(List<Category> e) {
        return e.stream()
                .map(category -> this.modelMapper.map(category, CategoryDto.class))
                .toList();
    }
}
