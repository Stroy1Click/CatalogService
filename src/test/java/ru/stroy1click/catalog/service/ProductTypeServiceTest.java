package ru.stroy1click.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.common.event.ProductTypeCreatedEvent;
import ru.stroy1click.common.event.ProductTypeDeletedEvent;
import ru.stroy1click.common.event.ProductTypeUpdatedEvent;
import ru.stroy1click.common.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.repository.ProductTypeRepository;
import ru.stroy1click.catalog.service.product.type.impl.ProductTypeServiceImpl;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductTypeMapper productTypeMapper;

    @Mock
    private CacheClear cacheClear;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StorageService storageService;

    @Mock
    private SubcategoryService subcategoryService;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private ProductTypeServiceImpl productTypeService;

    private ProductType productType;

    private ProductTypeDto productTypeDto;

    private Subcategory subcategory;

    private SubcategoryDto subcategoryDto;

    private final static String PRODUCT_TYPE_CREATED_TOPIC = "product-type-created-events";

    private final static String PRODUCT_TYPE_UPDATED_TOPIC = "product-type-updated-events";

    private final static String PRODUCT_TYPE_DELETED_TOPIC = "product-type-deleted-events";

    @BeforeEach
    public void setUp() {
        subcategory = Subcategory.builder()
                .id(5)
                .title("Phones")
                .image("link.png")
                .build();

        productType = ProductType.builder()
                .id(1)
                .title("Smartphones")
                .image("smart.png")
                .subcategory(subcategory)
                .build();

        subcategoryDto = SubcategoryDto.builder()
                .id(5)
                .title("Phones")
                .image("link.png")
                .build();

        productTypeDto = ProductTypeDto.builder()
                .id(1)
                .subcategoryId(5)
                .title("Smartphones")
                .title("smart.png")
                .build();
    }

    @Test
    public void get_WhenProductTypeExists_ShouldReturnProductTypeDto() {
        //Arrange
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(productType));
        when(this.productTypeMapper.toDto(productType)).thenReturn(productTypeDto);

        //Act
        ProductTypeDto result = this.productTypeService.get(1);

        //Assert
        assertThat(result).isEqualTo(productTypeDto);
        verify(productTypeRepository).findById(1);
    }

    @Test
    public void get_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productTypeRepository.findById(99)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("ProductType not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productTypeService.get(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("ProductType not found");
    }

    @Test
    public void create_WhenValidDataProvided_ShouldSaveAndReturnCreatedProductTypeAndSaveOutboxEvent() {
        //Arrange
        when(this.productTypeMapper.toEntity(productTypeDto)).thenReturn(productType);
        when(this.productTypeMapper.toDto(productType)).thenReturn(productTypeDto);
        when(this.subcategoryService.get(productTypeDto.getSubcategoryId())).thenReturn(subcategoryDto);
        when(this.productTypeRepository.save(productType)).thenReturn(productType);

        ProductTypeDto createdProductType = this.productTypeService.create(this.productTypeDto);

        assertNotNull(createdProductType.getId());
        assertEquals(productTypeDto.getTitle(), createdProductType.getTitle());
        verify(this.productTypeRepository).save(any(ProductType.class));
        verify(this.subcategoryService).get(productTypeDto.getSubcategoryId());
        verify(this.outboxEventService).save(eq(PRODUCT_TYPE_CREATED_TOPIC), any(ProductTypeCreatedEvent.class));
    }

    @Test
    public void create_WhenSubcategoryDoesNotExits_ShouldThrowNotFoundException() {
        //Arrange
        when(this.subcategoryService.get(productTypeDto.getSubcategoryId())).thenThrow(
                new NotFoundException("Subcategory not found")
        );

        //Act
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productTypeService.create(productTypeDto)
        );

        //Assert
        verify(this.subcategoryService).get(productTypeDto.getSubcategoryId());
        assertEquals("Subcategory not found", notFoundException.getMessage());
    }


    @Test
    public void update_WhenProductTypeExists_ShouldUpdateProductTypeAndSaveOutboxEvent() {
        //Arrange
        ProductTypeDto updatedDto = new ProductTypeDto(1, 5, "new.png", "New Type");
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(productType));
        when(this.productTypeRepository.save(any(ProductType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        this.productTypeService.update(1, updatedDto);

        //Assert
        ArgumentCaptor<ProductType> captor = ArgumentCaptor.forClass(ProductType.class);
        verify(this.productTypeRepository).save(captor.capture());
        ProductType saved = captor.getValue();
        verify(this.outboxEventService).save(eq(PRODUCT_TYPE_UPDATED_TOPIC), any(ProductTypeUpdatedEvent.class));
        assertThat(saved.getTitle()).isEqualTo("New Type");
        assertThat(saved.getSubcategory()).isEqualTo(subcategory);
    }


    @Test
    public void update_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("ProductType not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productTypeService.update(1, productTypeDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("ProductType not found");
    }

    @Test
    public void delete_WhenProductTypeExists_ShouldRemoveProductTypeAndClearCacheAndSaveOutboxEvent() {
        //Arrange
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(productType));

        //Act
        this.productTypeService.delete(1);

        //Assert
        verify(this.productTypeRepository).delete(productType);
        verify(this.outboxEventService).save(eq(PRODUCT_TYPE_DELETED_TOPIC), any(ProductTypeDeletedEvent.class));
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }

    @Test
    public void delete_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("ProductType not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productTypeService.delete(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void getByTitle_WhenProductTypeExists_ShouldReturnProductType() {
        //Arrange
        when(this.productTypeRepository.findByTitle("Smartphones")).thenReturn(Optional.of(productType));

        //Act
        Optional<ProductType> result = this.productTypeService.getByTitle("Smartphones");

        //Assert
        assertThat(result).contains(productType);
        verify(this.productTypeRepository).findByTitle("Smartphones");
    }

    @Test
    public void assignImage_WhenProductTypeExists_ShouldUploadImageAndClearCache() {
        //Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(productType));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        //Act
        this.productTypeService.assignImage(1, file);

        //Assert
        assertThat(this.productType.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }

    @Test
    public void assignImage_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(this.productTypeRepository.findById(99)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("ProductType not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productTypeService.assignImage(99, file))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void deleteImage_WhenProductTypeExists_ShouldDeleteImageAndClearCache() {
        //Arrange
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(productType));

        //Act
        this.productTypeService.deleteImage(1, "smart.png");

        //Assert
        assertThat(productType.getImage()).isNull();
        verify(this.storageService).deleteImage("smart.png");
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }

    @Test
    public void deleteImage_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("ProductType not found");

        //Act & Assert
       assertThrows(NotFoundException.class, () -> this.productTypeService.deleteImage(1, "smart.png"));
    }
}