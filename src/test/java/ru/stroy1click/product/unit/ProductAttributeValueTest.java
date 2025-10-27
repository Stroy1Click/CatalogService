package ru.stroy1click.product.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductAttributeValueDto;
import ru.stroy1click.product.entity.Attribute;
import ru.stroy1click.product.entity.Product;
import ru.stroy1click.product.entity.ProductAttributeValue;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductAttributeValueMapper;
import ru.stroy1click.product.repository.ProductAttributeValueRepository;
import ru.stroy1click.product.service.product.impl.ProductAttributeValueServiceImpl;


import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductAttributeValueTest {

    @Mock
    private ProductAttributeValueRepository productAttributeValueRepository;

    @Mock
    private ProductAttributeValueMapper mapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private CacheClear cacheClear;

    @InjectMocks
    private ProductAttributeValueServiceImpl productAttributeValueService;

    private ProductAttributeValue productAttributeValue;
    private ProductAttributeValueDto dto;
    private Product product;
    private Attribute attribute;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        this.product = Product.builder()
                .id(10)
                .title("Laptop")
                .build();

        this.attribute = Attribute.builder()
                .id(20)
                .title("Color")
                .build();

        this.productAttributeValue = ProductAttributeValue.builder()
                .id(1)
                .product(this.product)
                .attribute(this.attribute)
                .value("Black")
                .build();

        this.dto = new ProductAttributeValueDto(1, 20, 10, "Black");

        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Product attribute value not found");
    }

    @Test
    public void get_ShouldReturnDto_WhenExists() {
        when(this.productAttributeValueRepository.findById(1)).thenReturn(Optional.of(this.productAttributeValue));
        when(this.mapper.toDto(this.productAttributeValue)).thenReturn(this.dto);

        ProductAttributeValueDto result = this.productAttributeValueService.get(1);

        assertNotNull(result);
        assertEquals("Black", result.getValue());
        verify(this.productAttributeValueRepository).findById(1);
        verify(this.mapper).toDto(this.productAttributeValue);
    }

    @Test
    public void get_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productAttributeValueRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> this.productAttributeValueService.get(1));

        assertEquals("Product attribute value not found", exception.getMessage());
        verify(this.productAttributeValueRepository).findById(1);
        verify(this.mapper, never()).toDto(anyList());
    }

    @Test
    public void getAllByProductId_ShouldReturnList_WhenValuesExist() {
        when(this.productAttributeValueRepository.findByProduct_Id(10))
                .thenReturn(List.of(this.productAttributeValue));
        when(this.mapper.toDto(this.productAttributeValue)).thenReturn(this.dto);

        List<ProductAttributeValueDto> result = this.productAttributeValueService.getAllByProductId(10);

        assertEquals(1, result.size());
        verify(this.productAttributeValueRepository).findByProduct_Id(10);
        verify(this.mapper).toDto(this.productAttributeValue);
    }

    @Test
    public void getAllByProductId_ShouldReturnEmptyList_WhenNoValuesExist() {
        when(this.productAttributeValueRepository.findByProduct_Id(10)).thenReturn(List.of());

        List<ProductAttributeValueDto> result = this.productAttributeValueService.getAllByProductId(10);

        assertTrue(result.isEmpty());
        verify(this.productAttributeValueRepository).findByProduct_Id(10);
        verify(this.mapper, never()).toDto(anyList());
    }

    @Test
    public void getByValue_ShouldReturnOptional_WhenExists() {
        when(this.productAttributeValueRepository.findByValue("Black"))
                .thenReturn(Optional.of(this.productAttributeValue));

        Optional<ProductAttributeValue> result = this.productAttributeValueService.getByValue("Black");

        assertTrue(result.isPresent());
        assertEquals(this.productAttributeValue, result.get());
        verify(this.productAttributeValueRepository).findByValue("Black");
    }

    @Test
    public void getByValue_ShouldReturnEmptyOptional_WhenNotExists() {
        when(this.productAttributeValueRepository.findByValue("White")).thenReturn(Optional.empty());

        Optional<ProductAttributeValue> result = this.productAttributeValueService.getByValue("White");

        assertFalse(result.isPresent());
        verify(this.productAttributeValueRepository).findByValue("White");
    }

    @Test
    public void create_ShouldSaveEntity_WhenDtoIsValid() {
        ProductAttributeValue entity = this.productAttributeValue;
        when(this.mapper.toEntity(this.dto)).thenReturn(entity);

        this.productAttributeValueService.create(this.dto);

        verify(this.mapper).toEntity(this.dto);
        verify(this.productAttributeValueRepository).save(entity);
    }

    @Test
    public void delete_ShouldDeleteEntityAndClearCache_WhenExists() {
        when(this.productAttributeValueRepository.findById(1)).thenReturn(Optional.of(this.productAttributeValue));

        this.productAttributeValueService.delete(1);

        verify(this.cacheClear).clearAllProductAttributeValuesByProductId(10);
        verify(this.productAttributeValueRepository).delete(this.productAttributeValue);
    }

    @Test
    public void delete_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productAttributeValueRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> this.productAttributeValueService.delete(1));

        assertEquals("Product attribute value not found", exception.getMessage());
        verify(this.productAttributeValueRepository).findById(1);
        verify(this.productAttributeValueRepository, never()).delete(any());
        verify(this.cacheClear, never()).clearAllProductAttributeValuesByProductId(any());
    }

    @Test
    public void update_ShouldSaveUpdatedEntity_WhenExists() {
        ProductAttributeValue existing = this.productAttributeValue;
        ProductAttributeValueDto updateDto = new ProductAttributeValueDto(1, 20, 10, "White");

        when(this.productAttributeValueRepository.findById(1)).thenReturn(Optional.of(existing));

        this.productAttributeValueService.update(1, updateDto);

        ArgumentCaptor<ProductAttributeValue> captor = ArgumentCaptor.forClass(ProductAttributeValue.class);
        verify(this.productAttributeValueRepository).save(captor.capture());

        ProductAttributeValue saved = captor.getValue();
        assertEquals("White", saved.getValue());
        assertEquals(existing.getProduct(), saved.getProduct());
        assertEquals(existing.getAttribute(), saved.getAttribute());
    }

    @Test
    public void update_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.productAttributeValueRepository.findById(1)).thenReturn(Optional.empty());
        ProductAttributeValueDto updateDto = new ProductAttributeValueDto(1, 20, 10, "White");

        NotFoundException exception = assertThrows(NotFoundException.class, () -> this.productAttributeValueService.update(1, updateDto));

        assertEquals("Product attribute value not found", exception.getMessage());
        verify(this.productAttributeValueRepository).findById(1);
        verify(this.productAttributeValueRepository, never()).save(any());
        verify(this.cacheClear, never()).clearAllProductAttributeValuesByProductId(any());
    }
}

