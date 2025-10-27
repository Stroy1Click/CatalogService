package ru.stroy1click.product.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.entity.Category;
import ru.stroy1click.product.entity.Product;
import ru.stroy1click.product.entity.ProductType;
import ru.stroy1click.product.entity.Subcategory;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductMapper;
import ru.stroy1click.product.repository.ProductRepository;
import ru.stroy1click.product.service.product.ProductImageService;
import ru.stroy1click.product.service.product.impl.ProductServiceImpl;
import ru.stroy1click.product.service.storage.StorageService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CacheClear cacheClear;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StorageService storageService;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Category category = new Category();
        category.setId(1);

        Subcategory subcategory = new Subcategory();
        subcategory.setId(2);

        ProductType type = new ProductType();
        type.setId(3);

        this.product = Product.builder()
                .id(1)
                .title("Phone")
                .description("iPhone")
                .price(999.0)
                .inStock(true)
                .category(category)
                .subcategory(subcategory)
                .productType(type)
                .build();

        this.productDto = ProductDto.builder()
                .id(1)
                .title("Phone")
                .description("iPhone")
                .price(999.0)
                .inStock(true)
                .categoryId(1)
                .subcategoryId(2)
                .productTypeId(3)
                .build();

        when(this.messageSource.getMessage(eq("error.product.not_found"), any(), any()))
                .thenReturn("Product not found");
    }

    @Test
    public void get_ShouldReturnDto_WhenProductExists() {
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));
        when(this.productMapper.toDto(this.product)).thenReturn(this.productDto);

        ProductDto result = this.productService.get(1);

        assertThat(result).isEqualTo(this.productDto);
        verify(this.productMapper).toDto(this.product);
    }

    @Test
    public void get_ShouldThrowNotFoundException_WhenProductNotFound() {
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productService.get(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void create_ShouldSaveEntity_AndClearCache() {
        when(this.productMapper.toEntity(this.productDto)).thenReturn(this.product);

        this.productService.create(this.productDto);

        verify(this.productRepository).save(this.product);
        verify(this.cacheClear).clearPaginationOfProductsByCategory(1);
        verify(this.cacheClear).clearPaginationOfProductsBySubcategory(2);
        verify(this.cacheClear).clearPaginationOfProductsByProductType(3);
    }

    @Test
    public void update_ShouldUpdateExistingProduct() {
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));

        ProductDto updatedDto = ProductDto.builder()
                .title("NewPhone")
                .description("Updated")
                .price(1099.0)
                .inStock(true)
                .categoryId(1)
                .subcategoryId(2)
                .productTypeId(3)
                .build();

        this.productService.update(1, updatedDto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(this.productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("NewPhone");
        assertThat(saved.getCategory()).isEqualTo(this.product.getCategory());
        assertThat(saved.getSubcategory()).isEqualTo(this.product.getSubcategory());
        assertThat(saved.getProductType()).isEqualTo(this.product.getProductType());
    }

    @Test
    public void update_ShouldThrowNotFound_WhenIdNotExists() {
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productService.update(1, this.productDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void delete_ShouldRemoveProduct_AndClearCache() {
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));

        this.productService.delete(1);

        verify(this.productRepository).delete(this.product);
        verify(this.cacheClear).clearPaginationOfProductsByCategory(1);
        verify(this.cacheClear).clearPaginationOfProductsBySubcategory(2);
        verify(this.cacheClear).clearPaginationOfProductsByProductType(3);
    }

    @Test
    public void delete_ShouldThrowNotFound_WhenProductNotExists() {
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productService.delete(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void getByTitle_ShouldReturnOptionalProduct() {
        when(this.productRepository.findByTitle("Phone")).thenReturn(Optional.of(this.product));

        Optional<Product> result = this.productService.getByTitle("Phone");

        assertThat(result).isPresent();
        verify(this.productRepository).findByTitle("Phone");
    }

    @Test
    public void getByTitle_ShouldReturnEmpty_WhenNotExists() {
        when(this.productRepository.findByTitle("NonExisting")).thenReturn(Optional.empty());

        Optional<Product> result = this.productService.getByTitle("NonExisting");

        assertThat(result).isEmpty();
    }

    @Test
    public void assignImages_ShouldUploadAndAssignImages() {
        MultipartFile image = mock(MultipartFile.class);
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));
        when(this.storageService.uploadImages(List.of(image))).thenReturn(List.of("img1.png"));
        doNothing().when(this.productImageService).create(anyList());

        this.productService.assignImages(1, List.of(image));

        verify(this.productImageService).create(anyList());
        verify(this.cacheClear).clearPaginationOfProductsByCategory(1);
        verify(this.cacheClear).clearPaginationOfProductsBySubcategory(2);
        verify(this.cacheClear).clearPaginationOfProductsByProductType(3);
    }

    @Test
    public void assignImages_ShouldThrowNotFound_WhenProductNotExists() {
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productService.assignImages(1, List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void deleteImage_ShouldDeleteImageAndClearCache() {
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));

        this.productService.deleteImage(1, "img1.png");

        verify(this.productImageService).delete("img1.png");
        verify(this.cacheClear).clearPaginationOfProductsByCategory(1);
        verify(this.cacheClear).clearPaginationOfProductsBySubcategory(2);
        verify(this.cacheClear).clearPaginationOfProductsByProductType(3);
    }

    @Test
    public void deleteImage_ShouldThrowNotFound_WhenProductNotExists() {
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productService.deleteImage(1, "img.png"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }
}
