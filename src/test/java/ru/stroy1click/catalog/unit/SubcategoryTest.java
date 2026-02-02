package ru.stroy1click.catalog.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.mapper.SubcategoryMapper;
import ru.stroy1click.catalog.entity.MessageType;
import ru.stroy1click.catalog.repository.SubcategoryRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.outbox.OutboxMessageService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.impl.SubcategoryServiceImpl;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SubcategoryTest {

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
    private OutboxMessageService outboxMessageService;

    @InjectMocks
    private SubcategoryServiceImpl subcategoryService;

    private Subcategory subcategory;
    private SubcategoryDto subcategoryDto;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        this.category = Category.builder()
                .id(10)
                .title("Electronics")
                .image("link.png")
                .build();

        this.subcategory = Subcategory.builder()
                .id(1)
                .title("Smartphones")
                .image("phone.png")
                .category(this.category)
                .build();

        this.categoryDto = CategoryDto.builder()
                .id(10)
                .image("link.png")
                .build();

        this.subcategoryDto = SubcategoryDto.builder()
                .id(1)
                .categoryId(10)
                .title("Smartphones")
                .image("phone.png")
                .build();

        when(this.messageSource.getMessage(anyString(), any(), any()))
                .thenReturn("Subcategory not found");
    }

    @Test
    public void get_ShouldReturnSubcategoryDto_WhenExists() {
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));
        when(this.subcategoryMapper.toDto(this.subcategory)).thenReturn(this.subcategoryDto);

        SubcategoryDto result = this.subcategoryService.get(1);

        assertThat(result).isEqualTo(this.subcategoryDto);
        verify(this.subcategoryRepository).findById(1);
    }

    @Test
    public void get_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.subcategoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.subcategoryService.get(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subcategory not found");
    }

    @Test
    public void create_ShouldSaveEntity() {
        when(this.subcategoryMapper.toEntity(this.subcategoryDto)).thenReturn(this.subcategory);
        when(this.subcategoryMapper.toDto(this.subcategory)).thenReturn(this.subcategoryDto);
        when(this.categoryService.get(this.subcategoryDto.getCategoryId())).thenReturn(this.categoryDto);
        when(this.subcategoryRepository.save(this.subcategory)).thenReturn(this.subcategory);

        this.subcategoryService.create(this.subcategoryDto);

        ArgumentCaptor<Subcategory> captor = ArgumentCaptor.forClass(Subcategory.class);
        verify(this.subcategoryRepository).save(captor.capture());
        verify(this.categoryService).get(this.subcategoryDto.getCategoryId());
        verify(this.outboxMessageService)
                .save(this.subcategoryDto, MessageType.SUBCATEGORY_CREATED);
        assertThat(captor.getValue()).isEqualTo(this.subcategory);
    }

    @Test
    public void create_ShouldThrowNotFoundException_WhenCategoryNotExits() {
        when(this.subcategoryMapper.toEntity(this.subcategoryDto)).thenReturn(this.subcategory);
        when(this.categoryService.get(this.subcategoryDto.getCategoryId())).thenThrow(
                new NotFoundException("Category not found")
        );

        NotFoundException notFoundException = assertThrows(
                NotFoundException.class, () -> this.subcategoryService.create(this.subcategoryDto)
        );

        verify(this.categoryService).get(this.subcategoryDto.getCategoryId());
        assertEquals("Category not found", notFoundException.getMessage());
    }

    @Test
    public void update_ShouldUpdateExistingSubcategory() {
        SubcategoryDto updatedDto = new SubcategoryDto(1, 10, "new.png", "Phones");

        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));
        when(this.subcategoryRepository.save(any(Subcategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubcategoryDto outboxDto = new SubcategoryDto(1, 10, null, "Phones");
        when(this.subcategoryMapper.toDto(any(Subcategory.class))).thenReturn(outboxDto);

        this.subcategoryService.update(1, updatedDto);

        ArgumentCaptor<Subcategory> captor = ArgumentCaptor.forClass(Subcategory.class);
        verify(this.subcategoryRepository).save(captor.capture());
        Subcategory saved = captor.getValue();

        verify(this.outboxMessageService).save(outboxDto, MessageType.SUBCATEGORY_UPDATED);

        assertThat(saved.getTitle()).isEqualTo("Phones");
        assertThat(saved.getCategory()).isEqualTo(this.category);
        assertThat(saved.getImage()).isNull(); // как в твоём билдере
    }


    @Test
    public void update_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.subcategoryService.update(1, this.subcategoryDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subcategory not found");
    }

    @Test
    public void delete_ShouldRemoveSubcategoryAndClearCache() {
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));

        this.subcategoryService.delete(1);

        verify(this.subcategoryRepository).deleteById(1);
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
        verify(this.outboxMessageService).save(1, MessageType.SUBCATEGORY_DELETED);
    }

    @Test
    public void delete_ShouldThrowNotFoundException_WhenNotExists() {
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.subcategoryService.delete(1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void getByTitle_ShouldReturnSubcategory_WhenExists() {
        when(this.subcategoryRepository.findByTitle("Smartphones")).thenReturn(Optional.of(this.subcategory));

        Optional<Subcategory> result = this.subcategoryService.getByTitle("Smartphones");

        assertThat(result).contains(this.subcategory);
        verify(this.subcategoryRepository).findByTitle("Smartphones");
    }

    @Test
    public void assignImage_ShouldUploadImageAndClearCache() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));
        when(this.storageService.uploadImage(file)).thenReturn("newImage.png");

        this.subcategoryService.assignImage(1, file);

        assertThat(this.subcategory.getImage()).isEqualTo("newImage.png");
        verify(this.storageService).uploadImage(file);
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
    }

    @Test
    public void assignImage_ShouldThrowNotFoundException_WhenNotExists() {
        MultipartFile file = mock(MultipartFile.class);
        when(this.subcategoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.subcategoryService.assignImage(99, file))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void deleteImage_ShouldDeleteImageAndClearCache() {
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));

        this.subcategoryService.deleteImage(1, "phone.png");

        assertThat(this.subcategory.getImage()).isNull();
        verify(this.storageService).deleteImage("phone.png");
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
    }

    @Test
    public  void deleteImage_ShouldHandleNullImageGracefully() {
        this.subcategory.setImage(null);
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));

        assertThatCode(() -> this.subcategoryService.deleteImage(1, "any.png"))
                .doesNotThrowAnyException();

        verify(this.storageService).deleteImage("any.png");
        verify(this.cacheClear).clearSubcategoriesOfCategory(10);
    }

    @Test
    public void getSubcategories_ShouldReturnListOfDtos() {
        List<ProductType> productTypes = List.of(new ProductType(1,"title 1", "image 1",
                this.subcategory, List.of()), new ProductType(2,"title 2", "image 2",
                this.subcategory, List.of()));
        List<ProductTypeDto> productTypeDtos = List.of(new ProductTypeDto(1, 1,"image 1", "title 1"),
                new ProductTypeDto(2, 1,"image 2", "title 2"));
        this.subcategory.setProductTypes(productTypes);
        when(this.subcategoryRepository.findById(1)).thenReturn(Optional.of(this.subcategory));
        when(this.productTypeMapper.toDto(productTypes)).thenReturn(productTypeDtos);

        List<ProductTypeDto> result = this.subcategoryService.getProductTypes(1);


        verify(this.subcategoryRepository).findById(1);
        verify(this.productTypeMapper).toDto(productTypes);
        assertEquals(2, result.size());
    }

    @Test
    public void getSubcategories_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(this.subcategoryRepository.findById(999)).thenReturn(Optional.empty());
        when(this.messageSource.getMessage(anyString(), any(), any())).thenReturn("Subcategory not found");

        assertThatThrownBy(() -> this.subcategoryService.getProductTypes(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Subcategory not found");
    }
}

