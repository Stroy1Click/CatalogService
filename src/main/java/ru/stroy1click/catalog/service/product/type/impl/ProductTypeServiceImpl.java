package ru.stroy1click.catalog.service.product.type.impl;

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
import ru.stroy1click.catalog.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductTypeMapper;
import ru.stroy1click.catalog.entity.ProductType;
import ru.stroy1click.catalog.entity.MessageType;
import ru.stroy1click.catalog.repository.ProductTypeRepository;
import ru.stroy1click.catalog.service.outbox.OutboxMessageService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeServiceImpl implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeMapper productTypeMapper;

    private final CacheClear cacheClear;

    private final MessageSource messageSource;

    private final StorageService storageService;

    private final SubcategoryService subcategoryService;

    private final OutboxMessageService outboxMessageService;

    @Override
    @Cacheable(value = "productType", key = "#id")
    public ProductTypeDto get(Integer id) {
        log.info("get {}", id);

        return this.productTypeMapper.toDto(this.productTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_type.not_found",
                                null,
                                Locale.getDefault()
                        )
                )));
    }

    @Override
    @Cacheable(value = "allProductTypes")
    public List<ProductTypeDto> getAll() {
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

        this.outboxMessageService.save(createdProductType, MessageType.PRODUCT_TYPE_CREATED);

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
            ProductType updatedProductType = ProductType.builder()
                    .id(id)
                    .title(productTypeDto.getTitle())
                    .subcategory(productType.getSubcategory())
                    .products(productType.getProducts())
                    .build();

            this.productTypeRepository.save(updatedProductType);
            this.outboxMessageService.save(this.productTypeMapper.toDto(updatedProductType), MessageType.PRODUCT_TYPE_UPDATED);
        }, () -> {
            throw new NotFoundException(
                    this.messageSource.getMessage(
                            "error.product_type.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
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
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_type.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        this.productTypeRepository.delete(productType);
        this.outboxMessageService.save(id, MessageType.PRODUCT_TYPE_DELETED);

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
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_type.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));
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
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_type.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));
        this.storageService.deleteImage(imageName);
        productType.setImage(null);

        this.cacheClear.clearProductTypesOfSubcategory(productType.getSubcategory().getId());
    }
}