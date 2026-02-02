package ru.stroy1click.catalog.service.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.dto.ProductImageDto;
import ru.stroy1click.catalog.entity.Product;
import ru.stroy1click.catalog.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductMapper;
import ru.stroy1click.catalog.entity.MessageType;
import ru.stroy1click.catalog.repository.ProductRepository;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.service.outbox.OutboxMessageService;
import ru.stroy1click.catalog.service.product.ProductImageService;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.service.product.type.ProductTypeService;
import ru.stroy1click.catalog.service.storage.StorageService;
import ru.stroy1click.catalog.service.subcategory.SubcategoryService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final MessageSource messageSource;

    private final StorageService storageService;

    private final ProductImageService productImageService;

    private final CategoryService categoryService;

    private final SubcategoryService subcategoryService;

    private final ProductTypeService productTypeService;

    private final OutboxMessageService outboxMessageService;

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDto get(Integer id) {
        log.info("get {}", id);

        return this.productMapper.toDto(this.productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product.not_found",
                                null,
                                Locale.getDefault()
                        )
                )));
    }

    @Override
    @Cacheable(value = "allProducts")
    public List<ProductDto> getAll() {
        return this.productMapper.toDto(this.productRepository.findAll());
    }

    @Override
    @Transactional
    @CacheEvict(value = "allProducts", allEntries = true)
    public ProductDto create(ProductDto productDto) {
        log.info("create {}", productDto);

        CategoryDto categoryDto  = this.categoryService.get(productDto.getCategoryId());
        this.subcategoryService.get(productDto.getSubcategoryId());
        this.productTypeService.get(productDto.getProductTypeId());

        ProductDto createdProduct = this.productMapper.toDto(
                this.productRepository.save(this.productMapper.toEntity(productDto))
        );

        this.outboxMessageService.save(createdProduct, MessageType.PRODUCT_CREATED);

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
            Product updatedProduct = Product.builder()
                    .id(id)
                    .title(productDto.getTitle())
                    .description(productDto.getDescription())
                    .price(productDto.getPrice())
                    .inStock(productDto.getInStock())
                    .category(product.getCategory())
                    .subcategory(product.getSubcategory())
                    .productType(product.getProductType())
                    .build();

            this.productRepository.save(updatedProduct);
            this.outboxMessageService.save(this.productMapper.toDto(updatedProduct), MessageType.PRODUCT_UPDATED);
        }, () -> {
            throw new NotFoundException(
                    this.messageSource.getMessage(
                            "error.product.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
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
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        this.productRepository.delete(product);
        this.outboxMessageService.save(id, MessageType.PRODUCT_DELETED);
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
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));
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
                .orElseThrow(() -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product.not_found",
                                null,
                                Locale.getDefault()
                        )
                ));

        this.productImageService.delete(link);
    }
}