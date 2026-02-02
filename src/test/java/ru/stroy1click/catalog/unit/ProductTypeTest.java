package ru.stroy1click.catalog.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.entity.MessageType;
import ru.stroy1click.catalog.repository.ProductTypeRepository;
import ru.stroy1click.catalog.service.outbox.OutboxMessageService;
import ru.stroy1click.catalog.service.product.type.impl.ProductTypeServiceImpl;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductTypeTest {

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
    private OutboxMessageService outboxMessageService;

    @InjectMocks
    private ProductTypeServiceImpl productTypeService;

    private ProductType productType;
    private ProductTypeDto productTypeDto;
    private Subcategory subcategory;
    private SubcategoryDto subcategoryDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        this.subcategory = Subcategory.builder()
                .id(5)
                .title("Phones")
                .image("link.png")
                .build();

        this.productType = ProductType.builder()
                .id(1)
                .title("Smartphones")
                .image("smart.png")
                .subcategory(this.subcategory)
                .build();

        this.subcategoryDto = SubcategoryDto.builder()
                .id(5)
                .title("Phones")
                .image("link.png")
                .build();

        this.productTypeDto = ProductTypeDto.builder()
                .id(1)
                .subcategoryId(5)
                .title("Smartphones")
                .title("smart.png")
                .build();

        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("ProductType not found");
    }

    @Test
    public void get_ShouldReturnProductTypeDto_WhenExists() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));
        when(this.productTypeMapper.toDto(this.productType)).thenReturn(this.productTypeDto);

        ProductTypeDto result = this.productTypeService.get(1);

        assertThat(result).isEqualTo(this.productTypeDto);
        verify(this.productTypeRepository).findById(1);
    }

    @Test
    public void get_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productTypeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productTypeService.get(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("ProductType not found");
    }

    @Test
    public void create_ShouldSaveEntity() {
        when(this.productTypeMapper.toEntity(this.productTypeDto)).thenReturn(this.productType);
        when(this.productTypeMapper.toDto(this.productType)).thenReturn(this.productTypeDto);
        when(this.subcategoryService.get(this.productTypeDto.getSubcategoryId())).thenReturn(this.subcategoryDto);
        when(this.productTypeRepository.save(this.productType)).thenReturn(this.productType);

        this.productTypeService.create(this.productTypeDto);

        ArgumentCaptor<ProductType> captor = ArgumentCaptor.forClass(ProductType.class);
        verify(this.productTypeRepository).save(captor.capture());
        verify(this.subcategoryService).get(this.productTypeDto.getSubcategoryId());
        verify(this.outboxMessageService)
                .save(this.productTypeDto, MessageType.PRODUCT_TYPE_CREATED);
        assertThat(captor.getValue()).isEqualTo(this.productType);
    }

    @Test
    public void create_ShouldThrowNotFoundException_WhenSubcategoryNotExits() {
        when(this.productTypeMapper.toEntity(this.productTypeDto)).thenReturn(this.productType);
        when(this.subcategoryService.get(this.productTypeDto.getSubcategoryId())).thenThrow(
                new NotFoundException("Subcategory not found")
        );

        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productTypeService.create(this.productTypeDto)
        );

        verify(this.subcategoryService).get(this.productTypeDto.getSubcategoryId());
        assertEquals("Subcategory not found", notFoundException.getMessage());
    }


    @Test
    public void update_ShouldUpdateExistingProductType() {
        ProductTypeDto updatedDto = new ProductTypeDto(1, 5, "new.png", "New Type");

        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        when(this.productTypeRepository.save(any(ProductType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductTypeDto outboxDto = new ProductTypeDto(1, 5, null, "New Type");
        when(this.productTypeMapper.toDto(any(ProductType.class))).thenReturn(outboxDto);

        this.productTypeService.update(1, updatedDto);

        ArgumentCaptor<ProductType> captor = ArgumentCaptor.forClass(ProductType.class);
        verify(this.productTypeRepository).save(captor.capture());
        ProductType saved = captor.getValue();

        verify(this.outboxMessageService).save(outboxDto, MessageType.PRODUCT_TYPE_UPDATED);

        assertThat(saved.getTitle()).isEqualTo("New Type");
        assertThat(saved.getSubcategory()).isEqualTo(this.subcategory);
        assertThat(saved.getImage()).isNull(); // как в билдере
    }


    @Test
    public void update_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productTypeService.update(1, this.productTypeDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("ProductType not found");
    }

    @Test
    public void delete_ShouldRemoveProductTypeAndClearCache() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        this.productTypeService.delete(1);

        verify(this.productTypeRepository).delete(this.productType);
        verify(this.outboxMessageService).save(1,MessageType.PRODUCT_TYPE_DELETED);
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }

    @Test
    public void delete_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productTypeService.delete(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void getByTitle_ShouldReturnProductType_WhenExists() {
        when(this.productTypeRepository.findByTitle("Smartphones")).thenReturn(Optional.of(this.productType));

        Optional<ProductType> result = this.productTypeService.getByTitle("Smartphones");

        assertThat(result).contains(this.productType);
        verify(this.productTypeRepository).findByTitle("Smartphones");
    }

    @Test
    public void assignImage_ShouldUploadImageAndClearCache() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        this.productTypeService.assignImage(1, file);

        assertThat(this.productType.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }

    @Test
    public void assignImage_ShouldThrowNotFoundException_WhenNotExists() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.productTypeRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productTypeService.assignImage(99, file))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void deleteImage_ShouldDeleteImageAndClearCache() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        this.productTypeService.deleteImage(1, "smart.png");

        assertThat(this.productType.getImage()).isNull();
        verify(this.storageService).deleteImage("smart.png");
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }

    @Test
    public void deleteImage_ShouldHandleNullImageGracefully() {
        this.productType.setImage(null);
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        assertThatCode(() -> this.productTypeService.deleteImage(1, "any.png"))
                .doesNotThrowAnyException();

        verify(this.storageService).deleteImage("any.png");
        verify(this.cacheClear).clearProductTypesOfSubcategory(5);
    }
}

