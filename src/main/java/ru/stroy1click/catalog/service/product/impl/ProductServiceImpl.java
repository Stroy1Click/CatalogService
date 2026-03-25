package ru.stroy1click.catalog.service.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.dto.ProductImageDto;
import ru.stroy1click.catalog.entity.Product;
import ru.stroy1click.catalog.mapper.ProductMapper;
import ru.stroy1click.catalog.repository.ProductRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.product.ProductImageService;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;
import ru.stroy1click.common.event.ProductCreatedEvent;
import ru.stroy1click.common.event.ProductDeletedEvent;
import ru.stroy1click.common.event.ProductUpdatedEvent;
import ru.stroy1click.common.util.ExceptionUtils;
import ru.stroy1click.outbox.service.OutboxEventService;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final StorageService storageService;

    private final ProductImageService productImageService;

    private final CategoryService categoryService;

    private final SubcategoryService subcategoryService;

    private final ProductTypeService productTypeService;

    private final OutboxEventService outboxEventService;

    private final static String PRODUCT_CREATED_TOPIC = "product-created-events";

    private final static String PRODUCT_UPDATED_TOPIC = "product-updated-events";

    private final static String PRODUCT_DELETED_TOPIC = "product-deleted-events";

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDto get(Integer id) {
        log.info("get {}", id);

        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product.not_found",id));

        return this.productMapper.toDto(product);
    }

    @Override
    @Cacheable(value = "allProducts")
    public List<ProductDto> getAll() {
        log.info("getAll");

        return this.productMapper.toDto(this.productRepository.findAll());
    }

    @Override
    @Transactional
    @CacheEvict(value = "allProducts", allEntries = true)
    public ProductDto create(ProductDto productDto) {
        log.info("create {}", productDto);

        //проверка существований сущностей
        this.categoryService.get(productDto.getCategoryId());
        this.subcategoryService.get(productDto.getSubcategoryId());
        this.productTypeService.get(productDto.getProductTypeId());

        ProductDto createdProduct = this.productMapper.toDto(
                this.productRepository.save(this.productMapper.toEntity(productDto))
        );

        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .id(createdProduct.getId())
                .title(createdProduct.getTitle())
                .description(createdProduct.getDescription())
                .inStock(createdProduct.getInStock())
                .price(createdProduct.getPrice())
                .unit(createdProduct.getUnit())
                .categoryId(createdProduct.getCategoryId())
                .subcategoryId(createdProduct.getSubcategoryId())
                .productTypeId(createdProduct.getProductTypeId())
                .build();

        this.outboxEventService.save(PRODUCT_CREATED_TOPIC, event);

        return createdProduct;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void update(Integer id, ProductDto productDto) {
        log.info("update {}, {}", id, productDto);

        this.productRepository.findById(id).ifPresentOrElse(product -> {
            product.setTitle(productDto.getTitle());
            product.setUnit(productDto.getUnit());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setInStock(productDto.getInStock());

            ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                    .id(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .inStock(product.getInStock())
                    .price(product.getPrice())
                    .unit(product.getUnit())
                    .build();

            this.outboxEventService.save(PRODUCT_UPDATED_TOPIC, event);
        }, () -> {
            throw ExceptionUtils.notFound("error.product.not_found", id);
        });
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void delete(Integer id) {
        log.info("delete {}", id);

        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product.not_found",id));

        ProductDeletedEvent event = new ProductDeletedEvent(id);

        this.productRepository.delete(product);
        this.outboxEventService.save(PRODUCT_DELETED_TOPIC, event);
    }

    @Override
    public Optional<Product> getByTitle(String title) {
        log.info("getByTitle {}", title);

        return this.productRepository.findByTitle(title);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void assignImages(Integer id, List<MultipartFile> images) {
        log.info("assignImage {}", id);

        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product.not_found",id));

        List<ProductImageDto> imageNames = this.storageService.uploadImages(images)
                .stream()
                .map(imageName -> new ProductImageDto(
                        null,
                        product.getId(),
                        imageName
                ))
                .toList();

        this.productImageService.create(imageNames);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "allProducts", allEntries = true)
    })
    public void deleteImage(Integer id, String link) {
        log.info("deleteImage {} {}", id, link);

        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product.not_found",id));

        this.productImageService.delete(link);
    }
}