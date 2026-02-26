package ru.stroy1click.catalog.service.subcategory.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.common.event.SubcategoryCreatedEvent;
import ru.stroy1click.common.event.SubcategoryDeletedEvent;
import ru.stroy1click.common.event.SubcategoryUpdatedEvent;
import ru.stroy1click.common.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.mapper.SubcategoryMapper;
import ru.stroy1click.catalog.entity.Subcategory;
import ru.stroy1click.catalog.repository.SubcategoryRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubcategoryServiceImpl implements SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;

    private final SubcategoryMapper subcategoryMapper;

    private final ProductTypeMapper productTypeMapper;

    private final CacheClear cacheClear;

    private final MessageSource messageSource;

    private final StorageService storageService;

    private final CategoryService categoryService;

    private final OutboxEventService outboxEventService;

    private final static String SUBCATEGORY_CREATED_TOPIC = "subcategory-created-events";

    private final static String SUBCATEGORY_UPDATED_TOPIC = "subcategory-updated-events";

    private final static String SUBCATEGORY_DELETED_TOPIC = "subcategory-deleted-events";

    @Override
    @Cacheable(value = "subcategory", key = "#id")
    public SubcategoryDto get(Integer id) {
        log.info("get {}", id);

        return this.subcategoryMapper.toDto(this.subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.subcategory.not_found",
                                null,
                                Locale.getDefault()
                        )
                )));
    }

    @Override
    @Cacheable(value = "allSubcategories")
    public List<SubcategoryDto> getAll() {
        log.info("getAll");

        return this.subcategoryMapper.toDto(this.subcategoryRepository.findAll());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "subcategoriesOfCategory", key = "#subcategoryDto.categoryId"),
            @CacheEvict(value = "allSubcategories", allEntries = true)
    })
    public SubcategoryDto create(SubcategoryDto subcategoryDto) {
        log.info("create {}", subcategoryDto);

        this.categoryService.get(subcategoryDto.getCategoryId());

        SubcategoryDto createdSubcategory = this.subcategoryMapper.toDto(
                this.subcategoryRepository.save(this.subcategoryMapper.toEntity(subcategoryDto))
        );

        SubcategoryCreatedEvent event = SubcategoryCreatedEvent.builder()
                .id(createdSubcategory.getId())
                .categoryId(createdSubcategory.getCategoryId())
                .title(createdSubcategory.getTitle())
                .image(createdSubcategory.getImage())
                .build();

        this.outboxEventService.save(SUBCATEGORY_CREATED_TOPIC, event);

        return createdSubcategory;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "subcategory", key = "#id"),
            @CacheEvict(value = "subcategoriesOfCategory", key = "#subcategoryDto.categoryId"),
            @CacheEvict(value = "productTypesOfSubcategory", key = "#id"),
            @CacheEvict(value = "allSubcategories", allEntries = true)
    })
    public void update(Integer id, SubcategoryDto subcategoryDto) {
        log.info("update {}, {}", id, subcategoryDto);

        this.subcategoryRepository.findById(id).ifPresentOrElse(subcategory -> {
            Subcategory updatedSubcategory = Subcategory.builder()
                    .id(id)
                    .title(subcategoryDto.getTitle())
                    .image(subcategory.getImage())
                    .category(subcategory.getCategory())
                    .products(subcategory.getProducts())
                    .build();

            SubcategoryUpdatedEvent event = SubcategoryUpdatedEvent.builder()
                    .id(updatedSubcategory.getId())
                    .title(updatedSubcategory.getTitle())
                    .image(updatedSubcategory.getImage())
                    .build();

            this.subcategoryRepository.save(updatedSubcategory);
            this.outboxEventService.save(SUBCATEGORY_UPDATED_TOPIC, event);
        }, () -> {
            throw new NotFoundException(
                    this.messageSource.getMessage(
                            "error.subcategory.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
        });
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "subcategory", key = "#id"),
            @CacheEvict(value = "productTypesOfSubcategory", key = "#id"),
            @CacheEvict(value = "allSubcategories", allEntries = true),
            @CacheEvict(value = "allProductTypes", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void delete(Integer id) {
        log.info("delete {}", id);
        Subcategory subcategory = this.subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.subcategory.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        SubcategoryDeletedEvent event = new SubcategoryDeletedEvent(id);

        this.outboxEventService.save(SUBCATEGORY_DELETED_TOPIC, event);
        this.cacheClear.clearSubcategoriesOfCategory(subcategory.getCategory().getId());
        this.subcategoryRepository.deleteById(id);
    }

    @Override
    public Optional<Subcategory> getByTitle(String title) {
        log.info("getByTitle {}", title);
        return this.subcategoryRepository.findByTitle(title);
    }

    @Override
    @Cacheable(value = "productTypesOfSubcategory", key = "#id")
    public List<ProductTypeDto> getProductTypes(Integer id) {
        Subcategory subcategory = this.subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.subcategory.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        return this.productTypeMapper.toDto(subcategory.getProductTypes());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allSubcategories", allEntries = true),
            @CacheEvict(value = "subcategory", key = "#id"),
            @CacheEvict(value = "productTypesOfSubcategory", key = "#id")
    })
    public void assignImage(Integer id, MultipartFile image) {
        Subcategory subcategory = this.subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.subcategory.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));
        String imageName = this.storageService.uploadImage(image);
        subcategory.setImage(imageName);

        this.cacheClear.clearSubcategoriesOfCategory(subcategory.getCategory().getId());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "allSubcategories", allEntries = true),
            @CacheEvict(value = "subcategory", key = "#id"),
            @CacheEvict(value = "productTypesOfSubcategory", key = "#id")
    })
    public void deleteImage(Integer id, String imageName) {
        Subcategory subcategory = this.subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.subcategory.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));
        this.storageService.deleteImage(imageName);
        subcategory.setImage(null);

        this.cacheClear.clearSubcategoriesOfCategory(subcategory.getCategory().getId());
    }
}