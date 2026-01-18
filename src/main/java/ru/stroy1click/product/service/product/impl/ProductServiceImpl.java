package ru.stroy1click.product.service.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.dto.ProductImageDto;
import ru.stroy1click.product.entity.Product;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductMapper;
import ru.stroy1click.product.model.MessageType;
import ru.stroy1click.product.repository.ProductRepository;
import ru.stroy1click.product.service.category.CategoryService;
import ru.stroy1click.product.service.outbox.OutboxMessageService;
import ru.stroy1click.product.service.product.ProductImageService;
import ru.stroy1click.product.service.product.ProductService;
import ru.stroy1click.product.service.product.type.ProductTypeService;
import ru.stroy1click.product.service.storage.StorageService;
import ru.stroy1click.product.service.subcategory.SubcategoryService;

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

    private final CacheClear cacheClear;

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
    @Transactional
    public ProductDto create(ProductDto productDto) {
        log.info("create {}", productDto);

        this.categoryService.get(productDto.getCategoryId());
        this.subcategoryService.get(productDto.getSubcategoryId());
        this.productTypeService.get(productDto.getProductTypeId());

        ProductDto createdProduct = this.productMapper.toDto(
                this.productRepository.save(this.productMapper.toEntity(productDto))
        );

        this.outboxMessageService.save(createdProduct, MessageType.PRODUCT_CREATED);

        clearPaginationCache(productDto.getCategoryId(), productDto.getSubcategoryId(), productDto.getProductTypeId());

        return createdProduct;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "clearPaginationOfProductsByCategory", key = "#productDto.categoryId"),
            @CacheEvict(value = "clearPaginationOfProductsBySubcategory", key = "#productDto.subcategoryId"),
            @CacheEvict(value = "clearPaginationOfProductsByProductType", key = "#productDto.productTypeId"),
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
    @CacheEvict(value = "product", key = "#id")
    @Transactional
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

        clearPaginationCache(product.getCategory().getId(),
                product.getSubcategory().getId(),
                product.getProductType().getId());
    }

    @Override
    public Optional<Product> getByTitle(String title) {
        log.info("getByTitle {}", title);

        return this.productRepository.findByTitle(title);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "product", key = "#id")
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

        clearPaginationCache(product.getCategory().getId(),
                product.getSubcategory().getId(),
                product.getProductType().getId());

    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "product", key = "#id")
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

        clearPaginationCache(product.getCategory().getId(),
                product.getSubcategory().getId(),
                product.getProductType().getId());
    }

    private void clearPaginationCache(Integer categoryId, Integer subcategoryId, Integer productTypeId){
        this.cacheClear.clearPaginationOfProductsByCategory(categoryId);
        this.cacheClear.clearPaginationOfProductsBySubcategory(subcategoryId);
        this.cacheClear.clearPaginationOfProductsByProductType(productTypeId);
    }
}