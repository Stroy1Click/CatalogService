package ru.stroy1click.catalog.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Subcategory;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubcategoryMapper implements Mappable<Subcategory, SubcategoryDto>{

    private final ModelMapper modelMapper;

    @Override
    public Subcategory toEntity(SubcategoryDto subcategoryDto) {
        return this.modelMapper.map(subcategoryDto, Subcategory.class);
    }

    @Override
    public SubcategoryDto toDto(Subcategory subcategory) {
        return this.modelMapper.map(subcategory, SubcategoryDto.class);
    }

    @Override
    public List<SubcategoryDto> toDto(List<Subcategory> e) {
        return e.stream()
                .map(subcategory -> this.modelMapper.map(subcategory, SubcategoryDto.class))
                .toList();
    }
}
