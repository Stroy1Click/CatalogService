package ru.stroy1click.catalog.service.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.catalog.cache.CacheClear;
import ru.stroy1click.catalog.dto.ProductImageDto;
import ru.stroy1click.catalog.exception.NotFoundException;
import ru.stroy1click.catalog.mapper.ProductImageMapper;
import ru.stroy1click.catalog.entity.ProductImage;
import ru.stroy1click.catalog.repository.ProductImageRepository;
import ru.stroy1click.catalog.service.product.ProductImageService;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    private final ProductImageMapper productImageMapper;

    private final MessageSource messageSource;

    private final CacheClear cacheClear;

    @Override
    @Cacheable(cacheNames = "productImages", key = "#productId")
    public List<ProductImageDto> getAllByProductId(Integer productId) {
        return this.productImageMapper.toDto(
                this.productImageRepository.findAllByProduct_Id(productId)
        );
    }

    @Override
    public void create(List<ProductImageDto> list) {
        this.productImageRepository.saveAll(
                this.productImageMapper.toEntity(list)
        );
        this.cacheClear.clearProductImages(list.getFirst().getProductId());
    }

    @Override
    @CacheEvict(cacheNames = "productImages", key = "productImageDto.productId")
    public void create(ProductImageDto productImageDto) {
        this.productImageRepository.save(
                this.productImageMapper.toEntity(productImageDto)
        );
    }

    @Override
    @CacheEvict(cacheNames = "productImages", key = "productImageDto.productId")
    public void update(Integer id, ProductImageDto productImageDto) {
        this.productImageRepository.findById(id).ifPresentOrElse(productImage -> {
            ProductImage updatedProductImage = ProductImage.builder()
                    .id(id)
                    .link(productImageDto.getLink())
                    .product(productImage.getProduct())
                    .build();
            this.productImageRepository.save(updatedProductImage);
        }, () -> {
           throw new NotFoundException(
                   this.messageSource.getMessage(
                           "error.product_image",
                           null,
                           Locale.getDefault()
                   )
           );
        });
    }

    @Override
    public void delete(String link) {
        ProductImage productImage = this.productImageRepository.findByLink(link)
                .orElseThrow(() ->
                        new NotFoundException(
                                this.messageSource.getMessage(
                                        "error.product_image",
                                        null,
                                        Locale.getDefault()
                                )
                        )
                );
        this.cacheClear.clearProductImages(productImage.getProduct().getId());
        this.productImageRepository.delete(productImage);
    }
}