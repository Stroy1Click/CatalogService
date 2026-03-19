package ru.stroy1click.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.stroy1click.common.dto.Unit;
import ru.stroy1click.common.event.ProductCreatedEvent;
import ru.stroy1click.common.event.ProductDeletedEvent;
import ru.stroy1click.common.event.ProductUpdatedEvent;
import ru.stroy1click.common.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductMapper;
import ru.stroy1click.catalog.repository.ProductRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.product.ProductImageService;
import ru.stroy1click.catalog.service.product.impl.ProductServiceImpl;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

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
    private OutboxEventService outboxEventService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    private ProductDto productDto;

    private CategoryDto categoryDto;

    private SubcategoryDto subcategoryDto;

    private ProductTypeDto productTypeDto;

    private final static String PRODUCT_CREATED_TOPIC = "product-created-events";

    private final static String PRODUCT_UPDATED_TOPIC = "product-updated-events";

    private final static String PRODUCT_DELETED_TOPIC = "product-deleted-events";

    @BeforeEach
    public void setUp() {
        Category category = new Category();
        category.setId(1);

        Subcategory subcategory = new Subcategory();
        subcategory.setId(2);

        ProductType type = new ProductType();
        type.setId(3);

        categoryDto = CategoryDto.builder()
                .id(1)
                .title("Electronics")
                .image("image.png")
                .build();

        subcategoryDto = SubcategoryDto.builder()
                .id(2)
                .title("Phones")
                .image("image.png")
                .build();

        productTypeDto = ProductTypeDto.builder()
                .id(3)
                .title("Apple")
                .image("image.png")
                .build();

        product = Product.builder()
                .id(1)
                .title("Phone")
                .description("iPhone")
                .price(BigDecimal.valueOf(999.0))
                .unit(Unit.KG)
                .inStock(true)
                .category(category)
                .subcategory(subcategory)
                .productType(type)
                .build();

        productDto = ProductDto.builder()
                .id(1)
                .title("Phone")
                .description("iPhone")
                .price(BigDecimal.valueOf(999.0))
                .inStock(true)
                .unit(Unit.KG)
                .categoryId(1)
                .subcategoryId(2)
                .productTypeId(3)
                .build();
    }

    @Test
    public void get_WhenProductExists_ShouldReturnDto() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.productMapper.toDto(product)).thenReturn(productDto);

        //Act
        ProductDto result = this.productService.get(1);

        //Assert
        assertThat(result).isEqualTo(productDto);
        verify(this.productMapper).toDto(product);
    }

    @Test
    public void get_WhenProductDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product.not_found"), any(), any()))
                .thenReturn("Product not found");

        //Assert
        assertThatThrownBy(() -> this.productService.get(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void create_WhenDataIsValid_ShouldSaveAndReturnCreatedProductDtoAndSaveOutboxEvent() {
        //Arrange
        when(this.productMapper.toEntity(productDto)).thenReturn(product);
        when(this.productMapper.toDto(product)).thenReturn(productDto);
        when(this.categoryService.get(productDto.getCategoryId())).thenReturn(categoryDto);
        when(this.subcategoryService.get(productDto.getSubcategoryId())).thenReturn(subcategoryDto);
        when(this.productTypeService.get(productDto.getProductTypeId())).thenReturn(productTypeDto);
        when(this.productRepository.save(product)).thenReturn(product);

        //Act
        ProductDto createdProduct = this.productService.create(productDto);

        //Assert
        assertNotNull(createdProduct.getId());
        assertEquals(productDto.getTitle(), createdProduct.getTitle() );
        verify(this.productRepository).save(this.product);
        verify(this.outboxEventService).save(eq(PRODUCT_CREATED_TOPIC), any(ProductCreatedEvent.class));
    }

    @Test
    public void create_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryService.get(productDto.getCategoryId())).thenThrow(
                new NotFoundException("Category not found")
        );

        //Act
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productService.create(productDto)
        );

        //Assert
        verify(this.categoryService).get(productDto.getCategoryId());
        assertEquals("Category not found", notFoundException.getMessage());
    }

    @Test
    public void create_WhenSubcategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryService.get(productDto.getCategoryId())).thenReturn(categoryDto);
        when(this.subcategoryService.get(productDto.getSubcategoryId())).thenThrow(
                new NotFoundException("Subcategory not found")
        );

        //Act
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productService.create(productDto)
        );

        //Assert
        verify(this.categoryService).get(productDto.getCategoryId());
        assertEquals("Subcategory not found", notFoundException.getMessage());
    }

    @Test
    public void create_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryService.get(productDto.getCategoryId())).thenReturn(categoryDto);
        when(this.subcategoryService.get(productDto.getSubcategoryId())).thenReturn(subcategoryDto);
        when(this.productTypeService.get(productDto.getProductTypeId())).thenThrow(
                new NotFoundException("ProductType not found")
        );

        //Act
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.productService.create(productDto)
        );

        //Assert
        verify(this.categoryService).get(productDto.getCategoryId());
        assertEquals("ProductType not found", notFoundException.getMessage());
    }

    @Test
    public void update_WhenValidDataProvided_ShouldUpdateProductAndSaveOutboxEvent() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        ProductDto updatedDto = ProductDto.builder()
                .title("NewPhone")
                .description("Updated")
                .price(BigDecimal.valueOf(1099.0))
                .unit(Unit.KG)
                .inStock(true)
                .categoryId(1)
                .subcategoryId(2)
                .productTypeId(3)
                .build();

        //Act
        this.productService.update(1, updatedDto);

        //Assert
        verify(this.outboxEventService).save(eq(PRODUCT_UPDATED_TOPIC), any(ProductUpdatedEvent.class));
        assertEquals("NewPhone", product.getTitle());
        assertEquals("Updated", product.getDescription());
        assertEquals(Unit.KG, product.getUnit());
        assertEquals(BigDecimal.valueOf(1099.0), product.getPrice());
        assertTrue(product.getInStock());
    }


    @Test
    public void update_WhenProductDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product.not_found"), any(), any()))
                .thenReturn("Product not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productService.update(1, productDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void delete_WhenProductExists_ShouldRemoveProductAndClearCacheAndSaveOutboxEvent() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));

        //Act
        this.productService.delete(1);

        //Assert
        verify(this.productRepository).delete(product);
        verify(this.outboxEventService).save(eq(PRODUCT_DELETED_TOPIC), any(ProductDeletedEvent.class));
    }

    @Test
    public void delete_WhenProductDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product.not_found"), any(), any()))
                .thenReturn("Product not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productService.delete(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void getByTitle_WhenProductExists_ShouldReturnOptionalProduct() {
        //Arrange
        when(this.productRepository.findByTitle("Phone")).thenReturn(Optional.of(product));

        //Act
        Optional<Product> result = this.productService.getByTitle("Phone");

        //Assert
        assertThat(result).isPresent();
        verify(this.productRepository).findByTitle("Phone");
    }

    @Test
    public void getByTitle_WhenProductDoesNotExist_ShouldReturnEmptyOptional() {
        //Arrange
        when(this.productRepository.findByTitle("NonExisting")).thenReturn(Optional.empty());

        //Act
        Optional<Product> result = this.productService.getByTitle("NonExisting");

        //Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void assignImages_WhenProductExists_ShouldUploadAndAssignImages() {
        //Arrange
        MultipartFile image = mock(MultipartFile.class);
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        when(this.storageService.uploadImages(List.of(image))).thenReturn(List.of("img1.png"));
        doNothing().when(this.productImageService).create(anyList());

        //Act
        this.productService.assignImages(1, List.of(image));

        //Assert
        verify(this.productImageService).create(anyList());
    }

    @Test
    public void assignImages_WhenProductDoesNotExist_ShouldThrowNotFound() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product.not_found"), any(), any()))
                .thenReturn("Product not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productService.assignImages(1, List.of()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    public void deleteImage_WhenProductExists_ShouldDeleteImageAndClearCache() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));

        //Act
        this.productService.deleteImage(1, "img1.png");

        //Assert
        verify(this.productImageService).delete("img1.png");
    }

    @Test
    public void deleteImage_WhenProductDoesNotExist_ShouldThrowNotFound() {
        //Arrange
        when(this.productRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(eq("error.product.not_found"), any(), any()))
                .thenReturn("Product not found");

        //Act & Assert
        assertThatThrownBy(() -> this.productService.deleteImage(1, "img.png"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }
}