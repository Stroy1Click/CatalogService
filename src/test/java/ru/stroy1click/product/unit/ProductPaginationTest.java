package ru.stroy1click.product.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.exception.ValidationException;
import ru.stroy1click.product.model.ProductAttributeFilter;
import ru.stroy1click.product.repository.ProductRepository;
import ru.stroy1click.product.service.product.ProductService;
import ru.stroy1click.product.service.product.impl.ProductPaginationServiceImpl;

class ProductPaginationTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductPaginationServiceImpl productPaginationService;

    private ProductDto sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.sampleProduct = ProductDto.builder()
                .id(1)
                .title("Sample")
                .description("Description")
                .price(100.0)
                .inStock(true)
                .categoryId(1)
                .subcategoryId(1)
                .productTypeId(1)
                .build();
    }

    @Test
    void getProducts_byCategoryId() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Integer> productIds = List.of(1, 2, 3);
        Page<Integer> page = new PageImpl<>(productIds);

        when(this.productRepository.findProductIdsByCategory_Id(1, pageable)).thenReturn(page);
        when(this.productService.get(anyInt())).thenReturn(this.sampleProduct);

        List<ProductDto> result = this.productPaginationService.getProducts(1, null, null, pageable);

        assertEquals(3, result.size());
        verify(this.productRepository).findProductIdsByCategory_Id(1, pageable);
        for (Integer id : productIds) {
            verify(this.productService).get(id);
        }
    }

    @Test
    void testGetProducts_byCategoryId_ShouldReturnEmptyList_WhenNoProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Integer> emptyPage = new PageImpl<>(List.of());

        when(this.productRepository.findProductIdsByCategory_Id(1, pageable)).thenReturn(emptyPage);

        List<ProductDto> result = this.productPaginationService.getProducts(1, null, null, pageable);

        assertTrue(result.isEmpty());
        verify(this.productRepository).findProductIdsByCategory_Id(1, pageable);
        verify(this.productService, never()).get(anyInt());
    }

    @Test
    void getByFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductAttributeFilter filter = new ProductAttributeFilter();
        List<Integer> productIds = List.of(1, 2);
        Page<Integer> page = new PageImpl<>(productIds);

        when(this.productRepository.findIdsByAttributes(filter, pageable)).thenReturn(page);
        when(this.productService.get(anyInt())).thenReturn(this.sampleProduct);

        List<ProductDto> result = this.productPaginationService.getByFilter(filter, pageable);

        assertEquals(2, result.size());
        verify(this.productRepository).findIdsByAttributes(filter, pageable);
        for (Integer id : productIds) {
            verify(this.productService).get(id);
        }
    }

    @Test
    void getProducts_invalidCombination_categoryAndSubcategory_throwsValidationException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(this.messageSource.getMessage(eq("error.filter.invalid_combination"), any(), any(Locale.class)))
                .thenReturn("Invalid combination");

        assertThrows(ValidationException.class,
                () -> this.productPaginationService.getProducts(1, 1, null, pageable));
    }

    @Test
    void getProducts_invalidCombination_categoryAndProductType_throwsValidationException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(this.messageSource.getMessage(eq("error.filter.invalid_combination"), any(), any(Locale.class)))
                .thenReturn("Invalid combination");

        assertThrows(ValidationException.class,
                () -> this.productPaginationService.getProducts(1, null, 1, pageable));
    }

    @Test
    void getProducts_invalidCombination_allParameters_throwsValidationException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(this.messageSource.getMessage(eq("error.filter.invalid_combination"), any(), any(Locale.class)))
                .thenReturn("Invalid combination");

        assertThrows(ValidationException.class,
                () -> this.productPaginationService.getProducts(1, 1, 1, pageable));
    }

    @Test
    void getProducts_emptyFilter_throwsValidationException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(this.messageSource.getMessage(eq("error.filter.empty"), any(), any(Locale.class)))
                .thenReturn("Filter cannot be empty");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> this.productPaginationService.getProducts(null, null, null, pageable)
        );

        assertEquals("Filter cannot be empty", exception.getMessage());
    }

    @Test
    void getProducts_byCategoryId_WithDifferentPageable() {
        Pageable pageable = PageRequest.of(2, 5, Sort.by("title").descending());
        List<Integer> productIds = List.of(11, 12, 13, 14, 15);
        Page<Integer> page = new PageImpl<>(productIds);

        when(this.productRepository.findProductIdsByCategory_Id(1, pageable)).thenReturn(page);
        when(this.productService.get(anyInt())).thenReturn(this.sampleProduct);

        List<ProductDto> result = this.productPaginationService.getProducts(1, null, null, pageable);

        assertEquals(5, result.size());
        verify(this.productRepository).findProductIdsByCategory_Id(eq(1), any(Pageable.class));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(this.productRepository).findProductIdsByCategory_Id(eq(1), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(2, capturedPageable.getPageNumber());
        assertEquals(5, capturedPageable.getPageSize());
        assertEquals("title: DESC", capturedPageable.getSort().toString());
    }
}
