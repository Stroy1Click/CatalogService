package ru.stroy1click.product.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.entity.Category;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.CategoryMapper;
import ru.stroy1click.product.model.MessageType;
import ru.stroy1click.product.repository.CategoryRepository;
import ru.stroy1click.product.service.category.impl.CategoryServiceImpl;
import ru.stroy1click.product.service.outbox.OutboxMessageService;
import ru.stroy1click.product.service.storage.StorageService;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StorageService storageService;

    @Mock
    private OutboxMessageService outboxMessageService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        this.category = Category.builder()
                .id(1)
                .title("Electronics")
                .image("image.png")
                .build();

        this.categoryDto = new CategoryDto(1, "image.png", "Electronics");

        when(this.messageSource.getMessage(anyString(), any(), any())).thenReturn("Category not found");
    }

    @Test
    public void get_ShouldReturnCategoryDto_WhenExists() {
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(this.category));
        when(this.categoryMapper.toDto(this.category)).thenReturn(this.categoryDto);

        CategoryDto result = this.categoryService.get(1);

        assertThat(result).isEqualTo(this.categoryDto);
        verify(this.categoryRepository).findById(1);
    }

    @Test
    public void get_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.get(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    public void getAll_ShouldReturnListOfCategoryDtos() {
        when(this.categoryRepository.findAll()).thenReturn(List.of(this.category));
        when(this.categoryMapper.toDto(anyList())).thenReturn(List.of(this.categoryDto));

        List<CategoryDto> result = this.categoryService.getAll();

        assertThat(result).containsExactly(this.categoryDto);
    }

    @Test
    public void getAll_ShouldReturnEmptyList_WhenNoCategories() {
        when(this.categoryRepository.findAll()).thenReturn(Collections.emptyList());
        when(this.categoryMapper.toDto(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<CategoryDto> result = this.categoryService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    public void create_ShouldSaveEntity() {
        when(this.categoryMapper.toEntity(this.categoryDto)).thenReturn(this.category);
        when(this.categoryRepository.save(this.category)).thenReturn(this.category);
        when(this.categoryMapper.toDto(this.category)).thenReturn(this.categoryDto);

        this.categoryService.create(this.categoryDto);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(this.categoryRepository).save(captor.capture());
        verify(this.outboxMessageService)
                .save(this.categoryDto, MessageType.CATEGORY_CREATED);
        assertThat(captor.getValue()).isEqualTo(this.category);
    }

    @Test
    public void update_ShouldUpdateExistingCategory() {
        CategoryDto updatedDto = new CategoryDto(1, "new.png", "New Title");

        CategoryDto mappedDto =
                new CategoryDto(1, null, "New Title");

        when(this.categoryRepository.findById(1))
                .thenReturn(Optional.of(this.category));

        when(this.categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(this.categoryMapper.toDto(any(Category.class)))
                .thenReturn(mappedDto);

        this.categoryService.update(1, updatedDto);

        ArgumentCaptor<Category> categoryCaptor =
                ArgumentCaptor.forClass(Category.class);

        ArgumentCaptor<CategoryDto> dtoCaptor =
                ArgumentCaptor.forClass(CategoryDto.class);

        verify(this.categoryRepository).save(categoryCaptor.capture());
        verify(this.outboxMessageService)
                .save(dtoCaptor.capture(), eq(MessageType.CATEGORY_UPDATED));

        Category saved = categoryCaptor.getValue();
        CategoryDto eventDto = dtoCaptor.getValue();

        assertThat(saved.getId()).isEqualTo(1);
        assertThat(saved.getTitle()).isEqualTo("New Title");
        assertThat(saved.getProducts()).isEqualTo(this.category.getProducts());

        assertThat(eventDto.getId()).isEqualTo(1);
        assertThat(eventDto.getTitle()).isEqualTo("New Title");
    }

    @Test
    public void update_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(this.categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.update(1, this.categoryDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    public void delete_ShouldRemoveCategory_WhenExists() {
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(this.category));

        this.categoryService.delete(1);

        verify(this.categoryRepository).delete(this.category);
        verify(this.outboxMessageService).save(1, MessageType.CATEGORY_DELETED);
    }

    @Test
    public void delete_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.categoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.delete(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void assignImage_ShouldUploadAndAssignImage() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(this.category));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        this.categoryService.assignImage(1, file);

        assertThat(this.category.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
        verify(this.categoryRepository, never()).save(any());
    }

    @Test
    public void assignImage_ShouldThrowNotFoundException_WhenCategoryNotExists() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.categoryService.assignImage(99, file))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void deleteImage_ShouldDeleteImageAndClearField() {
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(this.category));

        this.categoryService.deleteImage(1, "image.png");

        assertThat(this.category.getImage()).isNull();
        verify(this.storageService).deleteImage("image.png");
    }

    @Test
    public void deleteImage_ShouldHandleNullImage() {
        this.category.setImage(null);
        when(this.categoryRepository.findById(1)).thenReturn(Optional.of(this.category));

        assertThatCode(() -> this.categoryService.deleteImage(1, "any.png"))
                .doesNotThrowAnyException();

        verify(this.storageService).deleteImage("any.png");
    }

    @Test
    public void getByTitle_ShouldReturnCategory_WhenExists() {
        when(this.categoryRepository.findByTitle("Electronics")).thenReturn(Optional.of(this.category));

        Optional<Category> result = this.categoryService.getByTitle("Electronics");

        assertThat(result).contains(this.category);
        verify(this.categoryRepository).findByTitle("Electronics");
    }
}