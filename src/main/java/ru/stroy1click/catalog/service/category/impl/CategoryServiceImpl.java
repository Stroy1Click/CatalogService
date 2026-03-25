package ru.stroy1click.catalog.service.category.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.entity.Category;
import ru.stroy1click.catalog.mapper.CategoryMapper;
import ru.stroy1click.catalog.mapper.SubcategoryMapper;
import ru.stroy1click.catalog.repository.CategoryRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.common.event.CategoryCreatedEvent;
import ru.stroy1click.common.event.CategoryDeletedEvent;
import ru.stroy1click.common.event.CategoryUpdatedEvent;
import ru.stroy1click.common.util.ExceptionUtils;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final SubcategoryMapper subcategoryMapper;

    private final StorageService storageService;

    private final OutboxEventService outboxEventService;

    private final static String CATEGORY_CREATED_TOPIC = "category-created-events";

    private final static String CATEGORY_UPDATED_TOPIC = "category-updated-events";

    private final static String CATEGORY_DELETED_TOPIC = "category-deleted-events";

    @Override
    @Cacheable(value = "category", key = "#id")
    public CategoryDto get(Integer id) {
        log.info("get {}", id);

        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.category.not_found",id));

        return this.categoryMapper.toDto(category);
    }

    @Override
    @Cacheable(value = "allCategories")
    public List<CategoryDto> getAll() {
        log.info("getAll");

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

        CategoryCreatedEvent event = CategoryCreatedEvent.builder()
                .id(createdCategory.getId())
                .title(createdCategory.getTitle())
                .image(createdCategory.getImage())
                .build();

        this.outboxEventService.save(CATEGORY_CREATED_TOPIC, event);

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
            category.setTitle(categoryDto.getTitle());

            CategoryUpdatedEvent event = CategoryUpdatedEvent.builder()
                    .id(category.getId())
                    .title(category.getTitle())
                    .image(category.getImage())
                    .build();

            this.outboxEventService.save(CATEGORY_UPDATED_TOPIC, event);
        }, () -> {
            throw ExceptionUtils.notFound("error.category.not_found", id);
        });
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "allCategories", allEntries = true),
            @CacheEvict(value = "allSubcategories", allEntries = true),
            @CacheEvict(value = "allProductTypes", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true),
            @CacheEvict(value = "subcategoriesOfCategory", key = "#id")
    })
    public void delete(Integer id) {
        log.info("delete {}", id);

        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.category.not_found",id));

        CategoryDeletedEvent event = new CategoryDeletedEvent(id);

        this.categoryRepository.delete(category);

        this.outboxEventService.save(CATEGORY_DELETED_TOPIC, event);
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
                .orElseThrow(() -> ExceptionUtils.notFound("error.category.not_found",id));

        return this.subcategoryMapper.toDto(category.getSubcategories());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "category", key = "#id"),
            @CacheEvict(value = "allCategories", allEntries = true)
    })
    public void assignImage(Integer id, MultipartFile image) {
        log.info("assignImage {}", id);
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.category.not_found",id));

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
        log.info("deleteImage {} {}", id, imageName);

        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.category.not_found",id));

        this.storageService.deleteImage(imageName);
        category.setImage(null);
    }
}
