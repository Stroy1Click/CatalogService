package ru.stroy1click.product.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductImageDto;
import ru.stroy1click.product.entity.Product;
import ru.stroy1click.product.entity.ProductImage;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductImageMapper;
import ru.stroy1click.product.repository.ProductImageRepository;
import ru.stroy1click.product.service.product.impl.ProductImageServiceImpl;

class ProductImageTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private CacheClear cacheClear;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private ProductImageDto sampleDto;
    private ProductImage sampleEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.sampleDto = new ProductImageDto();
        this.sampleDto.setId(1);
        this.sampleDto.setProductId(1);
        this.sampleDto.setLink("link1");

        this.sampleEntity = new ProductImage();
        this.sampleEntity.setId(1);
        this.sampleEntity.setLink("link1");
        this.sampleEntity.setProduct(new Product());
        this.sampleEntity.getProduct().setId(1);
    }

    @Test
    void getAllByProductId_ShouldReturnDtoList_WhenExists() {
        List<ProductImage> entities = List.of(this.sampleEntity);
        List<ProductImageDto> dtos = List.of(this.sampleDto);

        when(this.productImageRepository.findAllByProduct_Id(1)).thenReturn(entities);
        when(this.productImageMapper.toDto(entities)).thenReturn(dtos);

        List<ProductImageDto> result = this.productImageService.getAllByProductId(1);

        assertEquals(1, result.size());
        assertEquals("link1", result.getFirst().getLink());
        verify(this.productImageRepository).findAllByProduct_Id(1);
        verify(this.productImageMapper).toDto(entities);
    }

    @Test
    void getAllByProductId_ShouldReturnEmptyList_WhenNoImages() {
        when(this.productImageRepository.findAllByProduct_Id(1)).thenReturn(List.of());

        List<ProductImageDto> result = this.productImageService.getAllByProductId(1);

        assertTrue(result.isEmpty());
        verify(this.productImageRepository).findAllByProduct_Id(1);
        verify(this.productImageMapper).toDto(List.of());
    }

    @Test
    void createSingle_ShouldSave_WhenCalled() {
        ProductImage entity = this.sampleEntity;
        when(this.productImageMapper.toEntity(this.sampleDto)).thenReturn(entity);

        this.productImageService.create(this.sampleDto);

        verify(this.productImageMapper).toEntity(this.sampleDto);
        verify(this.productImageRepository).save(entity);
        verify(this.cacheClear, never()).clearProductImages(anyInt());
    }

    @Test
    void createList_ShouldSaveAllAndClearCache_WhenCalled() {
        List<ProductImageDto> dtoList = List.of(this.sampleDto);
        List<ProductImage> entityList = List.of(this.sampleEntity);

        when(this.productImageMapper.toEntity(dtoList)).thenReturn(entityList);

        this.productImageService.create(dtoList);

        verify(this.productImageMapper).toEntity(dtoList);
        verify(this.productImageRepository).saveAll(entityList);
        verify(this.cacheClear).clearProductImages(this.sampleDto.getProductId());
    }

    @Test
    void createList_ShouldClearCacheForFirstProduct_WhenMultipleProducts() {
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

        this.productImageService.create(dtoList);

        verify(this.productImageRepository).saveAll(entityList);
        verify(this.cacheClear).clearProductImages(1);
        verify(this.cacheClear, never()).clearProductImages(2);
    }

    @Test
    void update_ShouldSaveUpdatedEntity_WhenExists() {
        ProductImageDto updatedDto = new ProductImageDto();
        updatedDto.setLink("updatedLink");

        when(this.productImageRepository.findById(1)).thenReturn(Optional.of(this.sampleEntity));

        this.productImageService.update(1, updatedDto);

        ArgumentCaptor<ProductImage> captor = ArgumentCaptor.forClass(ProductImage.class);
        verify(this.productImageRepository).save(captor.capture());

        ProductImage saved = captor.getValue();
        assertEquals(1, saved.getId());
        assertEquals("updatedLink", saved.getLink());
        assertEquals(this.sampleEntity.getProduct(), saved.getProduct());
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenEntityDoesNotExist() {
        ProductImageDto updatedDto = new ProductImageDto();
        updatedDto.setLink("updatedLink");

        when(this.productImageRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product_image"), any(), any(Locale.class)))
                .thenReturn("Image not found");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> this.productImageService.update(1, updatedDto));

        assertEquals("Image not found", exception.getMessage());
    }

    @Test
    void update_ShouldNotClearCache_WhenSuccessful() {
        ProductImageDto updatedDto = new ProductImageDto();
        updatedDto.setLink("updatedLink");

        when(this.productImageRepository.findById(1)).thenReturn(Optional.of(this.sampleEntity));

        this.productImageService.update(1, updatedDto);

        verify(this.cacheClear, never()).clearProductImages(anyInt());
    }

    @Test
    void delete_ShouldDeleteEntityAndClearCache_WhenExists() {
        when(this.productImageRepository.findByLink("link1")).thenReturn(Optional.of(this.sampleEntity));

        this.productImageService.delete("link1");

        verify(this.cacheClear).clearProductImages(this.sampleEntity.getProduct().getId());
        verify(this.productImageRepository).delete(this.sampleEntity);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenEntityDoesNotExist() {
        when(this.productImageRepository.findByLink("link1")).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product_image"), any(), any(Locale.class)))
                .thenReturn("Image not found");

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> this.productImageService.delete("link1"));

        assertEquals("Image not found", exception.getMessage());
    }
}


