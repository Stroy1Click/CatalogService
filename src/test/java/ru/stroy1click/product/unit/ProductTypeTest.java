package ru.stroy1click.product.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductTypeDto;
import ru.stroy1click.product.entity.ProductType;
import ru.stroy1click.product.entity.Subcategory;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductTypeMapper;
import ru.stroy1click.product.repository.ProductTypeRepository;
import ru.stroy1click.product.service.product.type.impl.ProductTypeServiceImpl;
import ru.stroy1click.product.service.storage.StorageService;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
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

    @InjectMocks
    private ProductTypeServiceImpl productTypeService;

    private ProductType productType;
    private ProductTypeDto productTypeDto;
    private Subcategory subcategory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        this.subcategory = Subcategory.builder()
                .id(5)
                .title("Phones")
                .build();

        this.productType = ProductType.builder()
                .id(1)
                .title("Smartphones")
                .image("smart.png")
                .subcategory(this.subcategory)
                .build();

        this.productTypeDto = new ProductTypeDto(1, 5, "smart.png", "Smartphones");

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

        this.productTypeService.create(this.productTypeDto);

        ArgumentCaptor<ProductType> captor = ArgumentCaptor.forClass(ProductType.class);
        verify(this.productTypeRepository).save(captor.capture());
        assertThat(captor.getValue()).isEqualTo(this.productType);
    }

    @Test
    public void update_ShouldUpdateExistingProductType() {
        ProductTypeDto updatedDto = new ProductTypeDto(1, 5, "new.png", "New Type");
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        this.productTypeService.update(1, updatedDto);

        ArgumentCaptor<ProductType> captor = ArgumentCaptor.forClass(ProductType.class);
        verify(this.productTypeRepository).save(captor.capture());
        ProductType saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("New Type");
        assertThat(saved.getSubcategory()).isEqualTo(this.subcategory);
        assertThat(saved.getImage()).isNull(); // 🔥 image не сохраняется в update()
    }

    @Test
    public void update_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productTypeService.update(1, this.productTypeDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("ProductType not found");
    }

    // ---------- delete() ----------
    @Test
    public void delete_ShouldRemoveProductTypeAndClearCache() {
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        this.productTypeService.delete(1);

        verify(this.productTypeRepository).delete(this.productType);
        verify(this.cacheClear).clearProductsTypesOfSubcategory(5);
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
    public void getBySubcategory_ShouldReturnListOfDtos_WhenFound() {
        when(this.productTypeRepository.findBySubcategory_Id(5)).thenReturn(List.of(this.productType));
        when(this.productTypeMapper.toDto(this.productType)).thenReturn(this.productTypeDto);

        List<ProductTypeDto> result = this.productTypeService.getBySubcategory(5);

        assertThat(result).containsExactly(this.productTypeDto);
        verify(this.productTypeRepository).findBySubcategory_Id(5);
    }

    @Test
    public void getBySubcategory_ShouldThrowNotFoundException_WhenEmpty() {
        when(this.productTypeRepository.findBySubcategory_Id(5)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> this.productTypeService.getBySubcategory(5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("ProductType not found");
    }

    @Test
    public void assignImage_ShouldUploadImageAndClearCache() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        this.productTypeService.assignImage(1, file);

        assertThat(this.productType.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
        verify(this.cacheClear).clearProductsTypesOfSubcategory(5);
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
        verify(this.cacheClear).clearProductsTypesOfSubcategory(5);
    }

    @Test
    public void deleteImage_ShouldHandleNullImageGracefully() {
        this.productType.setImage(null);
        when(this.productTypeRepository.findById(1)).thenReturn(Optional.of(this.productType));

        assertThatCode(() -> this.productTypeService.deleteImage(1, "any.png"))
                .doesNotThrowAnyException();

        verify(this.storageService).deleteImage("any.png");
        verify(this.cacheClear).clearProductsTypesOfSubcategory(5);
    }
}

