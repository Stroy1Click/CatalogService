package ru.stroy1click.catalog.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.common.exception.ValidationException;
import ru.stroy1click.catalog.domain.common.dto.PageResponse;
import ru.stroy1click.catalog.domain.product.repository.ProductRepository;
import ru.stroy1click.catalog.domain.product.service.ProductService;
import ru.stroy1click.catalog.domain.product.service.impl.ProductPaginationServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductPaginationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductPaginationServiceImpl productPaginationService;

    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productDto = ProductDto.builder()
                .id(1)
                .title("Sample")
                .description("Description")
                .price(BigDecimal.valueOf(100.0))
                .inStock(true)
                .categoryId(1)
                .subcategoryId(1)
                .productTypeId(1)
                .build();
    }

    @Test
    void getProducts_WhenCategoryIdProvided_ShouldReturnProductIds() {
        //Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Integer> productIds = List.of(1, 2, 3);
        Page<Integer> page = new PageImpl<>(productIds);
        when(this.productRepository.findProductIdsByCategory_Id(1, pageable)).thenReturn(page);
        when(this.productService.get(anyInt())).thenReturn(productDto);

        //Act
        PageResponse<ProductDto> result = this.productPaginationService.getProducts(1, null, null, pageable);

        //Assert
        assertEquals(3, result.getTotalElements());
        verify(this.productRepository).findProductIdsByCategory_Id(1, pageable);
        verify(this.productService, times(3)).get(anyInt());
    }

    @Test
    void getProducts_WhenSubcategoryIdProvided_ShouldReturnProductIds() {
        //Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Integer> productIds = List.of(1, 2, 3);
        Page<Integer> page = new PageImpl<>(productIds);
        when(this.productRepository.findProductIdsBySubcategory_Id(1, pageable)).thenReturn(page);
        when(this.productService.get(anyInt())).thenReturn(productDto);

        //Act
        PageResponse<ProductDto> result = this.productPaginationService.getProducts(null, 1, null, pageable);

        //Assert
        assertEquals(3, result.getTotalElements());
        verify(this.productRepository).findProductIdsBySubcategory_Id(1, pageable);
        verify(this.productService, times(3)).get(anyInt());
    }

    @Test
    void getProducts_WhenProductTypeIdProvided_ShouldReturnProductIds() {
        //Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Integer> productIds = List.of(1, 2, 3);
        Page<Integer> page = new PageImpl<>(productIds);
        when(this.productRepository.findProductIdsByProductType_Id(1, pageable)).thenReturn(page);
        when(this.productService.get(anyInt())).thenReturn(productDto);

        //Act
        PageResponse<ProductDto> result = this.productPaginationService.getProducts(null, null, 1, pageable);

        //Assert
        assertEquals(3, result.getTotalElements());
        verify(this.productRepository).findProductIdsByProductType_Id(1, pageable);
        verify(this.productService, times(3)).get(anyInt());
    }

    @Test
    void getProducts_WhenCategoryIdProvidedAndProductIdsDoNotExists_ShouldReturnEmptyList() {
        //Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Integer> emptyPage = new PageImpl<>(List.of());
        when(this.productRepository.findProductIdsByCategory_Id(1, pageable)).thenReturn(emptyPage);

        //Act
        PageResponse<ProductDto> result = this.productPaginationService.getProducts(1, null, null, pageable);

        //Assert
        assertEquals(0L, result.getTotalElements());
        verify(this.productRepository).findProductIdsByCategory_Id(1, pageable);
        verify(this.productService, never()).get(anyInt());
    }

    @Test
    void getProducts_WhenNoArgumentProvided_ShouldThrowValidationException() {
        //Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(this.messageSource.getMessage(eq("error.filter.empty"), any(), any(Locale.class)))
                .thenReturn("Invalid combination");

        //Act & Assert
        assertThrows(ValidationException.class,
                () -> this.productPaginationService.getProducts(null, null, null, pageable));
    }
}