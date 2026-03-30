package ru.stroy1click.catalog.domain.producttype.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.domain.common.cache.CacheClear;
import ru.stroy1click.catalog.domain.producttype.dto.ProductTypeDto;
import ru.stroy1click.catalog.domain.producttype.entity.ProductType;
import ru.stroy1click.catalog.domain.producttype.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.domain.producttype.repository.ProductTypeRepository;
import ru.stroy1click.catalog.domain.producttype.service.ProductTypeService;
import ru.stroy1click.catalog.domain.common.service.StorageService;
import ru.stroy1click.catalog.domain.subcategory.service.SubcategoryService;
import ru.stroy1click.common.event.ProductTypeCreatedEvent;
import ru.stroy1click.common.event.ProductTypeDeletedEvent;
import ru.stroy1click.common.event.ProductTypeUpdatedEvent;
import ru.stroy1click.common.util.ExceptionUtils;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeMapper productTypeMapper;

    private final CacheClear cacheClear;

    private final StorageService storageService;

    private final SubcategoryService subcategoryService;

    private final OutboxEventService outboxEventService;

    private final static String PRODUCT_TYPE_CREATED_TOPIC = "product-type-created-events";

    private final static String PRODUCT_TYPE_UPDATED_TOPIC = "product-type-updated-events";

    private final static String PRODUCT_TYPE_DELETED_TOPIC = "product-type-deleted-events";

    @Override
    @Cacheable(value = "productType", key = "#id")
    public ProductTypeDto get(Integer id) {
        log.info("get {}", id);

        ProductType productType = this.productTypeRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product_type.not_found",id));

        return this.productTypeMapper.toDto(productType);
    }

    @Override
    @Cacheable(value = "allProductTypes")
    public List<ProductTypeDto> getAll() {
        log.info("getAll");

        return this.productTypeMapper.toDto(this.productTypeRepository.findAll());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productTypesOfSubcategory", key = "#productTypeDto.subcategoryId"),
            @CacheEvict(value = "allProductTypes", allEntries = true)
    })
    public ProductTypeDto create(ProductTypeDto productTypeDto) {
        log.info("create {}", productTypeDto);

        this.subcategoryService.get(productTypeDto.getSubcategoryId());

        ProductTypeDto createdProductType = this.productTypeMapper.toDto(
                this.productTypeRepository.save(this.productTypeMapper.toEntity(productTypeDto))
        );

        ProductTypeCreatedEvent event = ProductTypeCreatedEvent.builder()
                .id(createdProductType.getId())
                .subcategoryId(createdProductType.getSubcategoryId())
                .title(createdProductType.getTitle())
                .image(createdProductType.getImage())
                .build();

        this.outboxEventService.save(PRODUCT_TYPE_CREATED_TOPIC, event);

        return createdProductType;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productType", key = "#id"),
            @CacheEvict(value = "productTypesOfSubcategory", key = "#productTypeDto.subcategoryId"),
            @CacheEvict(value = "allProductTypes", allEntries = true)
    })
    public void update(Integer id, ProductTypeDto productTypeDto) {
        log.info("update {}, {}", id, productTypeDto);

        this.productTypeRepository.findById(id).ifPresentOrElse(productType -> {
            productType.setTitle(productTypeDto.getTitle());

            ProductTypeUpdatedEvent event = ProductTypeUpdatedEvent.builder()
                    .id(productType.getId())
                    .title(productType.getTitle())
                    .image(productType.getImage())
                    .build();

            this.outboxEventService.save(PRODUCT_TYPE_UPDATED_TOPIC, event);
        }, () -> {
            throw ExceptionUtils.notFound("error.product_type.not_found",id);
        });
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productType", key = "#id"),
            @CacheEvict(value = "allProductTypes", allEntries = true),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void delete(Integer id) {
        log.info("delete {}", id);

        ProductType productType = this.productTypeRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product_type.not_found",id));

        ProductTypeDeletedEvent event = new ProductTypeDeletedEvent(id);

        this.productTypeRepository.delete(productType);
        this.outboxEventService.save(PRODUCT_TYPE_DELETED_TOPIC, event);

        this.cacheClear.clearProductTypesOfSubcategory(productType.getSubcategory().getId());
    }

    @Override
    public Optional<ProductType> getByTitle(String title) {
        return this.productTypeRepository.findByTitle(title);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "productType", key = "#id"),
            @CacheEvict(value = "allProductTypes", allEntries = true)
    })
    public void assignImage(Integer id, MultipartFile image) {
        log.info("assignImage {}", id);

        ProductType productType = this.productTypeRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product_type.not_found",id));

        String imageName = this.storageService.uploadImage(image);
        productType.setImage(imageName);

        this.cacheClear.clearProductTypesOfSubcategory(productType.getSubcategory().getId());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "productType", key = "#id"),
            @CacheEvict(value = "allProductTypes", allEntries = true)
    })
    public void deleteImage(Integer id, String imageName) {
        log.info("deleteImage {}, {}", id, imageName);

        ProductType productType = this.productTypeRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product_type.not_found",id));

        this.storageService.deleteImage(imageName);
        productType.setImage(null);

        this.cacheClear.clearProductTypesOfSubcategory(productType.getSubcategory().getId());
    }
}