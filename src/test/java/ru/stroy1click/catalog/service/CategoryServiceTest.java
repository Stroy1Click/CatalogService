package ru.stroy1click.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.mapper.CategoryMapper;
import ru.stroy1click.catalog.mapper.SubcategoryMapper;
import ru.stroy1click.catalog.repository.CategoryRepository;
import ru.stroy1click.catalog.service.category.impl.CategoryServiceImpl;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.common.event.CategoryCreatedEvent;
import ru.stroy1click.common.event.CategoryDeletedEvent;
import ru.stroy1click.common.event.CategoryUpdatedEvent;
import ru.stroy1click.common.exception.NotFoundException;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SubcategoryMapper subcategoryMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StorageService storageService;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    private CategoryDto categoryDto;

    private final static String CATEGORY_CREATED_TOPIC = "category-created-events";
    private final static String CATEGORY_UPDATED_TOPIC = "category-updated-events";
    private final static String CATEGORY_DELETED_TOPIC = "category-deleted-events";

    @BeforeEach
    public void setUp() {
        category = Category.builder()
                .id(1)
                .title("Electronics")
                .image("image.png")
                .build();

        categoryDto = new CategoryDto(1, "image.png", "Electronics");
    }

    @Test
    public void get_WhenCategoryExists_ShouldReturnCategoryDto() {
        //Arrange
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(this.categoryMapper.toDto(category)).thenReturn(categoryDto);

        //Act
        CategoryDto result = this.categoryService.get(1);

        //Assert
        assertThat(result).isEqualTo(categoryDto);
        verify(this.categoryRepository).findById(1);
    }

    @Test
    public void get_WhenCategoryNotExists_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryRepository.findById(99)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Category not found");

        //Act & Assert
        assertThatThrownBy(() -> this.categoryService.get(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    public void getAll_WhenCategoriesExist_ShouldReturnListOfCategoryDtos() {
        //Arrange
        when(this.categoryRepository.findAll()).thenReturn(List.of(category));
        when(this.categoryMapper.toDto(anyList())).thenReturn(List.of(categoryDto));

        //Act
        List<CategoryDto> result = this.categoryService.getAll();

        //Assert
        assertThat(result).containsExactly(categoryDto);
    }

    @Test
    public void getAll_ShouldReturnEmptyList_WhenNoCategories() {
        //Arrange
        when(this.categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(this.categoryMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());

        //Act
        List<CategoryDto> result = this.categoryService.getAll();

        //Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void create_WhenValidData_ShouldReturnSavedCategoryDtoAndSaveOutboxEvent() {
        //Arrange
        when(this.categoryMapper.toEntity(categoryDto)).thenReturn(category);
        when(this.categoryRepository.save(category)).thenReturn(category);
        when(this.categoryMapper.toDto(category)).thenReturn(categoryDto);

        //Act
        CategoryDto createdCategory = this.categoryService.create(categoryDto);

        //Assert
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        assertNotNull(createdCategory.getId());
        assertEquals(categoryDto.getTitle(), createdCategory.getTitle() );
        verify(this.categoryRepository).save(captor.capture());
        verify(this.outboxEventService).save(eq(CATEGORY_CREATED_TOPIC), any(CategoryCreatedEvent.class));
        assertThat(captor.getValue()).isEqualTo(category);
    }

    @Test
    public void update_WhenValidData_ShouldUpdateExistingCategoryAndSaveOutboxEvent() {
        //Arrange
        CategoryDto updatedDto = new CategoryDto(1, "image.png", "New Title");
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(category));

        //Act
        this.categoryService.update(1, updatedDto);

        //Assert
        verify(this.outboxEventService).save(eq(CATEGORY_UPDATED_TOPIC), any(CategoryUpdatedEvent.class));
        assertEquals("New Title",category.getTitle());
    }

    @Test
    public void update_WhenCategoryNotFound_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Category not found");

        //Act & Assert
        assertThatThrownBy(() -> this.categoryService.update(1, categoryDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    public void delete_WhenCategoryExists_ShouldRemoveCategoryAndSaveOutboxEvent() {
        //Arrange
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(category));

        //Act
        this.categoryService.delete(1);

        //Assert
        verify(this.categoryRepository).delete(category);
        verify(this.outboxEventService).save(eq(CATEGORY_DELETED_TOPIC), any(CategoryDeletedEvent.class));
    }

    @Test
    public void delete_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryRepository.findById(1)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Category not found");

        //Act & Assert
        assertThatThrownBy(() -> this.categoryService.delete(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void assignImage_WhenCategoryExists_ShouldUploadAndAssignImage() {
        //Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        //Act
        this.categoryService.assignImage(1, file);

        //Assert
        assertThat(category.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
    }

    @Test
    public void assignImage_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(this.categoryRepository.findById(99)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Category not found");

        //Act & Assert
        assertThatThrownBy(() -> this.categoryService.assignImage(99, file))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void deleteImage_WhenCategoryExists_ShouldDeleteImage() {
        //Arrange
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(category));

        //Act
        this.categoryService.deleteImage(1, "image.png");

        //Assert
        assertThat(category.getImage()).isNull();
        verify(this.storageService).deleteImage("image.png");
    }

    @Test
    public void getByTitle_ShouldReturnCategory_WhenExists() {
        //Arrange
        when(this.categoryRepository.findByTitle("Electronics")).thenReturn(Optional.of(category));

        //Act
        Optional<Category> result = this.categoryService.getByTitle("Electronics");

        //Assert
        assertThat(result).contains(category);
        verify(this.categoryRepository).findByTitle("Electronics");
    }

    @Test
    public void getSubcategories_WhenCategoryExists_ShouldReturnListOfDtos() {
        //Arrange
        List<Subcategory> subcategories = List.of(new Subcategory(1,"title 1", "image 1",
                category, List.of(), List.of()),  new Subcategory(2,"title 2", "image 2",
                category, List.of(), List.of()));
        List<SubcategoryDto> subcategoryDtos = List.of(new SubcategoryDto(1, 1,"image 1", "title 1"),
                new SubcategoryDto(2, 1,"image 2", "title 2"));
        category.setSubcategories(subcategories);
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(this.subcategoryMapper.toDto(subcategories)).thenReturn(subcategoryDtos);

        //Act
        List<SubcategoryDto> result = this.categoryService.getSubcategories(1);

        //Assert
        verify(this.categoryRepository).findById(1);
        verify(this.subcategoryMapper).toDto(subcategories);
        assertEquals(2, result.size());
    }

    @Test
    public void getSubcategories_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        when(this.categoryRepository.findById(999)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any())).thenReturn("Category not found");
        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Category not found");

        //Act & Assert
        assertThatThrownBy(() -> this.categoryService.getSubcategories(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");
    }
}