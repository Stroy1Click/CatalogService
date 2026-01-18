package ru.stroy1click.product.service.category.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.dto.SubcategoryDto;
import ru.stroy1click.product.entity.Category;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.CategoryMapper;
import ru.stroy1click.product.mapper.SubcategoryMapper;
import ru.stroy1click.product.model.MessageType;
import ru.stroy1click.product.repository.CategoryRepository;
import ru.stroy1click.product.service.category.CategoryService;
import ru.stroy1click.product.service.outbox.OutboxMessageService;
import ru.stroy1click.product.service.storage.StorageService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final SubcategoryMapper subcategoryMapper;

    private final MessageSource messageSource;

    private final StorageService storageService;

    private final OutboxMessageService outboxMessageService;

    @Override
    @Cacheable(value = "category", key = "#id")
    public CategoryDto get(Integer id) {
        log.info("get {}", id);

        return this.categoryMapper.toDto(this.categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.category.not_found",
                                null,
                                Locale.getDefault()
                        )
                )));
    }

    @Override
    @Cacheable(value = "allCategories")
    public List<CategoryDto> getAll() {
        return this.categoryMapper.toDto(this.categoryRepository.findAll());
    }

    @Override
    @Transactional
    @CacheEvict(value = "allCategories", allEntries = true)
    public CategoryDto create(CategoryDto categoryDto) {
        log.info("create {}", categoryDto);

        CategoryDto createdCategory = this.categoryMapper.toDto(
                this.categoryRepository.save(this.categoryMapper.toEntity(categoryDto))
        );

        this.outboxMessageService.save(createdCategory, MessageType.CATEGORY_CREATED);

        return createdCategory;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "allCategories", allEntries = true)
    })
    public void update(Integer id, CategoryDto categoryDto) {
        log.info("update {}, {}", id, categoryDto);

        this.categoryRepository.findById(id).ifPresentOrElse(category -> {
            Category updateCategory = Category.builder()
                    .id(id)
                    .title(categoryDto.getTitle())
                    .products(category.getProducts())
                    .build();

            this.categoryRepository.save(updateCategory);
            this.outboxMessageService.save(this.categoryMapper.toDto(updateCategory), MessageType.CATEGORY_UPDATED);
        }, () -> {
            throw new NotFoundException(
                    this.messageSource.getMessage(
                            "error.category.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
        });
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "allCategories", allEntries = true),
            @CacheEvict(value = "subcategoriesOfCategory", key = "#id")
    })
    public void delete(Integer id) {
        log.info("delete {}", id);

        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.category.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        this.categoryRepository.delete(category);
        this.outboxMessageService.save(id, MessageType.CATEGORY_DELETED);
    }

    @Override
    public Optional<Category> getByTitle(String title) {
        log.info("getByTitle {}", title);

        return this.categoryRepository.findByTitle(title);
    }

    @Override
    @Cacheable(value = "subcategoriesOfCategory", key = "#id")
    public List<SubcategoryDto> getSubcategories(Integer id) {
        log.info("getSubcategories {}", id);

        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.category.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        return this.subcategoryMapper.toDto(category.getSubcategories());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "allCategories", allEntries = true)
    })
    public void assignImage(Integer id, MultipartFile image) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.category.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        String imageName = this.storageService.uploadImage(image);
        category.setImage(imageName);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "allCategories", allEntries = true)
    })
    public void deleteImage(Integer id, String imageName) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.category.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        this.storageService.deleteImage(imageName);
        category.setImage(null);
    }
}
