package ru.stroy1click.catalog.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.entity.Category;

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
