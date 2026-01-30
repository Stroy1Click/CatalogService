package ru.stroy1click.catalog.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.entity.Product;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductMapper;
import ru.stroy1click.catalog.model.MessageType;
import ru.stroy1click.catalog.repository.ProductRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.outbox.OutboxMessageService;
import ru.stroy1click.catalog.service.product.ProductImageService;
import ru.stroy1click.catalog.service.product.impl.ProductServiceImpl;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StorageService storageService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private SubcategoryService subcategoryService;

    @Mock
    private ProductTypeService productTypeService;

    @Mock
    private OutboxMessageService outboxMessageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;
    private CategoryDto categoryDto;
    private SubcategoryDto subcategoryDto;
    private ProductTypeDto productTypeDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Category category = new Category();
        category.setId(1);

        Subcategory subcategory = new Subcategory();
        subcategory.setId(2);

        ProductType type = new ProductType();
        type.setId(3);

        this.categoryDto = CategoryDto.builder()
                .id(1)
                .title("Electronics")
                .image("image.png")
                .build();

        this.subcategoryDto = SubcategoryDto.builder()
                .id(2)
                .title("Phones")
                .image("image.png")
                .build();

        this.productTypeDto = ProductTypeDto.builder()
                .id(3)
                .title("Apple")
                .image("image.png")
                .build();

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
    public void create_ShouldSaveEntity() {
        when(this.productMapper.toEntity(this.productDto)).thenReturn(this.product);
        when(this.productMapper.toDto(this.product)).thenReturn(this.productDto);
        when(this.categoryService.get(this.productDto.getCategoryId())).thenReturn(this.categoryDto);
        when(this.subcategoryService.get(this.productDto.getSubcategoryId())).thenReturn(this.subcategoryDto);
        when(this.productTypeService.get(this.productDto.getProductTypeId())).thenReturn(this.productTypeDto);
        when(this.productRepository.save(this.product)).thenReturn(this.product);

        this.productService.create(this.productDto);

        verify(this.productRepository).save(this.product);
        verify(this.outboxMessageService)
                .save(this.productDto, MessageType.PRODUCT_CREATED);
    }

    @Test
    public void create_ShouldThrowNotFoundException_WhenCategoryNotExits() {
        when(this.productMapper.toEntity(this.productDto)).thenReturn(this.product);
        when(this.categoryService.get(this.productDto.getCategoryId())).thenThrow(
                new NotFoundException("Category not found")
        );

        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productService.create(this.productDto)
        );

        verify(this.categoryService).get(this.productDto.getCategoryId());
        assertEquals("Category not found", notFoundException.getMessage());
    }

    @Test
    public void create_ShouldThrowNotFoundException_WhenSubcategoryNotExits() {
        when(this.productMapper.toEntity(this.productDto)).thenReturn(this.product);
        when(this.categoryService.get(this.productDto.getCategoryId())).thenReturn(this.categoryDto);
        when(this.subcategoryService.get(this.productDto.getSubcategoryId())).thenThrow(
                new NotFoundException("Subcategory not found")
        );

        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productService.create(this.productDto)
        );

        verify(this.categoryService).get(this.productDto.getCategoryId());
        assertEquals("Subcategory not found", notFoundException.getMessage());
    }

    @Test
    public void create_ShouldThrowNotFoundException_WhenProductTypeNotExits() {
        when(this.productMapper.toEntity(this.productDto)).thenReturn(this.product);
        when(this.categoryService.get(this.productDto.getCategoryId())).thenReturn(this.categoryDto);
        when(this.subcategoryService.get(this.productDto.getSubcategoryId())).thenReturn(this.subcategoryDto);
        when(this.productTypeService.get(this.productDto.getProductTypeId())).thenThrow(
                new NotFoundException("ProductType not found")
        );

        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productService.create(this.productDto)
        );

        verify(this.categoryService).get(this.productDto.getCategoryId());
        assertEquals("ProductType not found", notFoundException.getMessage());
    }

    @Test
    public void update_ShouldUpdateExistingProduct() {
        when(this.productRepository.findById(1)).thenReturn(Optional.of(this.product));

        when(this.productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductDto updatedDto = ProductDto.builder()
                .title("NewPhone")
                .description("Updated")
                .price(1099.0)
                .inStock(true)
                .categoryId(1)
                .subcategoryId(2)
                .productTypeId(3)
                .build();

        ProductDto outboxDto = ProductDto.builder()
                .title("NewPhone")
                .description("Updated")
                .price(1099.0)
                .inStock(true)
                .categoryId(1)
                .subcategoryId(2)
                .productTypeId(3)
                .build();
        when(this.productMapper.toDto(any(Product.class))).thenReturn(outboxDto);

        this.productService.update(1, updatedDto);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(this.productRepository).save(captor.capture());
        Product saved = captor.getValue();

        verify(this.outboxMessageService).save(outboxDto, MessageType.PRODUCT_UPDATED);

        assertThat(saved.getTitle()).isEqualTo("NewPhone");
        assertThat(saved.getCategory()).isEqualTo(this.product.getCategory());
        assertThat(saved.getSubcategory()).isEqualTo(this.product.getSubcategory());
        assertThat(saved.getProductType()).isEqualTo(this.product.getProductType());
        assertThat(saved.getDescription()).isEqualTo("Updated");
        assertThat(saved.getPrice()).isEqualTo(1099.0);
        assertThat(saved.getInStock()).isTrue();
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
        verify(this.outboxMessageService).save(1,MessageType.PRODUCT_DELETED);
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
    }

    @Test
    public void deleteImage_ShouldThrowNotFound_WhenProductNotExists() {
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.productService.deleteImage(1, "img.png"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }
}
