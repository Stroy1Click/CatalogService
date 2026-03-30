package ru.stroy1click.catalog.domain.product.image.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.catalog.domain.common.cache.CacheClear;
import ru.stroy1click.catalog.domain.product.image.dto.ProductImageDto;
import ru.stroy1click.catalog.domain.product.image.entity.ProductImage;
import ru.stroy1click.catalog.domain.product.image.mapper.ProductImageMapper;
import ru.stroy1click.catalog.domain.product.image.repository.ProductImageRepository;
import ru.stroy1click.catalog.domain.product.image.service.ProductImageService;
import ru.stroy1click.common.util.ExceptionUtils;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    private final ProductImageMapper productImageMapper;

    private final CacheClear cacheClear;

    @Override
    @Cacheable(cacheNames = "productImages", key = "#productId")
    public List<ProductImageDto> getAllByProductId(Integer productId) {
        log.info("getAllByProductId {}", productId);

        return this.productImageMapper.toDto(
                this.productImageRepository.findAllByProduct_Id(productId)
        );
    }

    @Override
    public void create(List<ProductImageDto> list) {
        log.info("create list {}", list);

        this.productImageRepository.saveAll(
                this.productImageMapper.toEntity(list)
        );

        this.cacheClear.clearProductImages(list.getFirst().getProductId());
    }

    @Override
    @CacheEvict(cacheNames = "productImages", key = "#productImageDto.productId")
    public void create(ProductImageDto productImageDto) {
        log.info("create {}", productImageDto);

        this.productImageRepository.save(
                this.productImageMapper.toEntity(productImageDto)
        );
    }

    @Override
    @CacheEvict(cacheNames = "productImages", key = "#productImageDto.productId")
    public void update(Integer id, ProductImageDto productImageDto) {
        log.info("update {}, {}", id, productImageDto);

        this.productImageRepository.findById(id).ifPresentOrElse(productImage -> {
            productImage.setLink(productImageDto.getLink());
        }, () -> {
            throw ExceptionUtils.notFound("error.product_image", id);
        });
    }

    @Override
    public void delete(String link) {
        log.info("delete {}", link);

        ProductImage productImage = this.productImageRepository.findByLink(link)
                .orElseThrow(() -> ExceptionUtils.notFound("error.product_image.not_found", link));

        this.cacheClear.clearProductImages(productImage.getProduct().getId());
        this.productImageRepository.delete(productImage);
    }
}