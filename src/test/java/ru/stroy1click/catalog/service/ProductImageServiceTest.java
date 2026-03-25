package ru.stroy1click.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.ProductImageDto;
import ru.stroy1click.catalog.entity.Product;
import ru.stroy1click.catalog.entity.ProductImage;
import ru.stroy1click.catalog.mapper.ProductImageMapper;
import ru.stroy1click.catalog.repository.ProductImageRepository;
import ru.stroy1click.catalog.service.product.impl.ProductImageServiceImpl;
import ru.stroy1click.common.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private CacheClear cacheClear;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private ProductImageDto productImageDto;

    private ProductImage productImage;

    @BeforeEach
    void setUp() {
        productImageDto = new ProductImageDto();
        productImageDto.setId(1);
        productImageDto.setProductId(1);
        productImageDto.setLink("link1");

        productImage = new ProductImage();
        productImage.setId(1);
        productImage.setLink("link1");
        productImage.setProduct(new Product());
        productImage.getProduct().setId(1);
    }

    @Test
    void getAllByProductId_WhenProductHasImages_ShouldReturnDtoList() {
        //Arrange
        List<ProductImage> entities = List.of(productImage);
        List<ProductImageDto> dtos = List.of(productImageDto);
        when(this.productImageRepository.findAllByProduct_Id(1)).thenReturn(entities);
        when(this.productImageMapper.toDto(entities)).thenReturn(dtos);

        //Act
        List<ProductImageDto> result = this.productImageService.getAllByProductId(1);

        //Assert
        assertEquals(1, result.size());
        assertEquals("link1", result.getFirst().getLink());
        verify(this.productImageRepository).findAllByProduct_Id(1);
        verify(this.productImageMapper).toDto(entities);
    }

    @Test
    void getAllByProductId_WhenProductDoesNotHaveImages_ShouldReturnEmptyList() {
        //Arrange
        when(this.productImageRepository.findAllByProduct_Id(1)).thenReturn(List.of());

        //Act
        List<ProductImageDto> result = this.productImageService.getAllByProductId(1);

        //Assert
        assertTrue(result.isEmpty());
        verify(this.productImageRepository).findAllByProduct_Id(1);
        verify(this.productImageMapper).toDto(List.of());
    }

    @Test
    void create_WhenValidData_ShouldSaveImage() {
        //Arrange
        ProductImage entity = productImage;
        when(this.productImageMapper.toEntity(productImageDto)).thenReturn(entity);

        //Act
        this.productImageService.create(productImageDto);

        //Assert
        verify(this.productImageMapper).toEntity(productImageDto);
        verify(this.productImageRepository).save(entity);
        verify(this.cacheClear, never()).clearProductImages(anyInt());
    }

    @Test
    void create_WhenValidData_ShouldSaveImagesAndClearCache() {
        //Arrange
        ProductImageDto dto1 = new ProductImageDto();
        dto1.setId(1);
        dto1.setProductId(1);
        dto1.setLink("link1");

        ProductImageDto dto2 = new ProductImageDto();
        dto2.setId(2);
        dto2.setProductId(2);
        dto2.setLink("link2");

        List<ProductImageDto> dtoList = List.of(dto1, dto2);

        ProductImage entity1 = new ProductImage();
        entity1.setId(1);
        entity1.setProduct(new Product());
        entity1.getProduct().setId(1);
        entity1.setLink("link1");

        ProductImage entity2 = new ProductImage();
        entity2.setId(2);
        entity2.setProduct(new Product());
        entity2.getProduct().setId(2);
        entity2.setLink("link2");

        List<ProductImage> entityList = List.of(entity1, entity2);

        when(this.productImageMapper.toEntity(dtoList)).thenReturn(entityList);

        //Act
        this.productImageService.create(dtoList);

        //Assert
        verify(this.productImageRepository).saveAll(entityList);
        verify(this.cacheClear).clearProductImages(1);
        verify(this.cacheClear, never()).clearProductImages(2);
    }

    @Test
    void update_WhenImageExists_ShouldSaveUpdatedEntity() {
        //Arrange
        ProductImageDto updatedDto = new ProductImageDto();
        updatedDto.setLink("updatedLink");
        when(this.productImageRepository.findById(1)).thenReturn(Optional.of(productImage));

        //Act
        this.productImageService.update(1, updatedDto);

        //Assert
        assertEquals(1, productImage.getId());
        assertEquals("updatedLink", productImage.getLink());
    }

    @Test
    void update_WhenImageDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        ProductImageDto updatedDto = new ProductImageDto();
        updatedDto.setLink("updatedLink");
        when(this.productImageRepository.findById(1)).thenReturn(Optional.empty());

        //Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> this.productImageService.update(1, updatedDto));

        //Assert
        assertEquals("error.product_image", exception.getMessage());
    }

    @Test
    void delete_WhenImageExists_ShouldDeleteEntityAndClearCache() {
        //Arrange
        when(this.productImageRepository.findByLink("link1")).thenReturn(Optional.of(productImage));

        //Act
        this.productImageService.delete("link1");

        //Assert
        verify(this.cacheClear).clearProductImages(productImage.getProduct().getId());
        verify(this.productImageRepository).delete(productImage);
    }

    @Test
    void delete_WhenImageDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productImageRepository.findByLink("link1")).thenReturn(Optional.empty());

        //Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> this.productImageService.delete("link1"));

        //Assert
        assertEquals("error.product_image.not_found", exception.getMessage());
    }
}


