package ru.stroy1click.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.common.event.SubcategoryCreatedEvent;
import ru.stroy1click.common.event.SubcategoryDeletedEvent;
import ru.stroy1click.common.event.SubcategoryUpdatedEvent;
import ru.stroy1click.common.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.mapper.SubcategoryMapper;
import ru.stroy1click.catalog.repository.SubcategoryRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.impl.SubcategoryServiceImpl;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubcategoryServiceTest {

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private SubcategoryMapper subcategoryMapper;

    @Mock
    private ProductTypeMapper productTypeMapper;

    @Mock
    private CacheClear cacheClear;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StorageService storageService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private SubcategoryServiceImpl subcategoryService;

    private Subcategory subcategory;

    private SubcategoryDto subcategoryDto;

    private Category category;

    private CategoryDto categoryDto;

    private final static String SUBCATEGORY_CREATED_TOPIC = "subcategory-created-events";

    private final static String SUBCATEGORY_UPDATED_TOPIC = "subcategory-updated-events";

    private final static String SUBCATEGORY_DELETED_TOPIC = "subcategory-deleted-events";

    @BeforeEach
    public void setUp() {
        category = Category.builder()
                .id(10)
                .title("Electronics")
                .image("link.png")
                .build();

        subcategory = Subcategory.builder()
                .id(1)
                .title("Smartphones")
                .image("phone.png")
                .category(category)
                .build();

        categoryDto = CategoryDto.builder()
                .id(10)
                .image("link.png")
                .build();

        subcategoryDto = SubcategoryDto.builder()
                .id(1)
                .categoryId(10)
                .title("Smartphones")
                .image("phone.png")
                .build();
    }

    @Test
    public void get_WhenSubcategoryExists_ShouldReturnSubcategoryDto() {
        //Arrange
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(subcategory));
        when(this.subcategoryMapper.toDto(subcategory)).thenReturn(subcategoryDto);

        //Act
        SubcategoryDto result = this.subcategoryService.get(1);

        //Assert
        assertThat(result).isEqualTo(subcategoryDto);
        verify(this.subcategoryRepository).findById(1);
    }

    @Test
    public void get_WhenSubcategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.subcategoryRepository.findById(99)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Subcategory not found");

        //Act & Assert
        assertThatThrownBy(() -> this.subcategoryService.get(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subcategory not found");
    }

    @Test
    public void create_WhenValiDataProvided_ShouldSaveAndReturnCreatedSubcategoryAndSaveOutboxEvent() {
        //Arrange
        when(this.subcategoryMapper.toEntity(subcategoryDto)).thenReturn(subcategory);
        when(this.subcategoryMapper.toDto(subcategory)).thenReturn(subcategoryDto);
        when(this.categoryService.get(subcategoryDto.getCategoryId())).thenReturn(categoryDto);
        when(this.subcategoryRepository.save(subcategory)).thenReturn(subcategory);

        //Act
        SubcategoryDto createdSubcategory = this.subcategoryService.create(subcategoryDto);

        //Assert
        assertNotNull(createdSubcategory.getId());
        assertEquals(subcategoryDto.getTitle(), createdSubcategory.getTitle());
        verify(this.categoryService).get(subcategoryDto.getCategoryId());
        verify(this.outboxEventService).save(eq(SUBCATEGORY_CREATED_TOPIC), any(SubcategoryCreatedEvent.class));
    }

    @Test
    public void create_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryService.get(subcategoryDto.getCategoryId())).thenThrow(
                new NotFoundException("Category not found")
        );

        //Act
        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.subcategoryService.create(subcategoryDto)
        );

        //Assert
        verify(this.categoryService).get(subcategoryDto.getCategoryId());
        assertEquals("Category not found", notFoundException.getMessage());
    }

    @Test
    public void update_WhenSubcategoryExists_ShouldUpdateSubcategoryAndSaveOutboxEvent() {
        //Arrange
        SubcategoryDto updatedDto = new SubcategoryDto(1, 10, "new.png", "Phones");
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(subcategory));
        when(this.subcategoryRepository.save(any(Subcategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        this.subcategoryService.update(1, updatedDto);

        //Assert
        ArgumentCaptor<Subcategory> captor = ArgumentCaptor.forClass(Subcategory.class);
        verify(this.subcategoryRepository).save(captor.capture());
        Subcategory saved = captor.getValue();
        verify(this.outboxEventService).save(eq(SUBCATEGORY_UPDATED_TOPIC), any(SubcategoryUpdatedEvent.class));
        assertThat(saved.getTitle()).isEqualTo("Phones");
    }


    @Test
    public void update_WhenSubcategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Subcategory not found");

        //Act & Assert
        assertThatThrownBy(() -> this.subcategoryService.update(1, subcategoryDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subcategory not found");
    }

    @Test
    public void delete_WhenSubcategoryExists_ShouldRemoveSubcategoryAndClearCache() {
        //Arrange
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(subcategory));

        //Act
        this.subcategoryService.delete(1);

        //Assert
        verify(this.subcategoryRepository).deleteById(1);
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
        verify(this.outboxEventService).save(eq(SUBCATEGORY_DELETED_TOPIC), any(SubcategoryDeletedEvent.class));
    }

    @Test
    public void delete_WhenSubcategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Subcategory not found");

        //Act & Assert
        assertThatThrownBy(() -> this.subcategoryService.delete(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void getByTitle_WhenSubcategoryExists_ShouldReturnSubcategory() {
        //Arrange
        when(this.subcategoryRepository.findByTitle("Smartphones")).thenReturn(Optional.of(subcategory));

        //Act
        Optional<Subcategory> result = this.subcategoryService.getByTitle("Smartphones");

        //Assert
        assertThat(result).contains(subcategory);
        verify(this.subcategoryRepository).findByTitle("Smartphones");
    }

    @Test
    public void assignImage_WhenSubcategoryExists_ShouldUploadImageAndClearCache() {
        //Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(subcategory));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        //Act
        this.subcategoryService.assignImage(1, file);

        //Assert
        assertThat(subcategory.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
    }

    @Test
    public void assignImage_WhenSubcategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Act
        MultipartFile file = mock(MultipartFile.class);
        when(this.subcategoryRepository.findById(99)).thenReturn(Optional.empty());

        //Assert & Act
        assertThatThrownBy(() -> this.subcategoryService.assignImage(99, file))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void deleteImage_WhenSubcategoryExists_ShouldDeleteImageAndClearCache() {
        //Arrange
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(subcategory));
        doNothing().when(this.storageService).deleteImage(anyString());

        //Act
        this.subcategoryService.deleteImage(1, "phone.png");

        //Assert
        assertThat(this.subcategory.getImage()).isNull();
        verify(this.storageService).deleteImage("phone.png");
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
    }


    @Test
    public void getProductTypes_WhenSubcategoryExists_ShouldReturnListOfDtos() {
        //Arrange
        List<ProductType> productTypes = List.of(new ProductType(1,"title 1", "image 1",
                subcategory, List.of()), new ProductType(2,"title 2", "image 2",
                subcategory, List.of()));
        List<ProductTypeDto> productTypeDtos = List.of(new ProductTypeDto(1, 1,"image 1", "title 1"),
                new ProductTypeDto(2, 1,"image 2", "title 2"));
        this.subcategory.setProductTypes(productTypes);
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(subcategory));
        when(this.productTypeMapper.toDto(productTypes)).thenReturn(productTypeDtos);

        //Act
        List<ProductTypeDto> result = this.subcategoryService.getProductTypes(1);

        //Assert
        verify(this.subcategoryRepository).findById(1);
        verify(this.productTypeMapper).toDto(productTypes);
        assertEquals(2, result.size());
    }

    @Test
    public void getSubcategories_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        //Arrange
        when(this.subcategoryRepository.findById(999)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any())).thenReturn("Subcategory not found");

        //Act & Assert
        assertThatThrownBy(() -> this.subcategoryService.getProductTypes(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subcategory not found");
    }
}