package ru.stroy1click.product.service.product.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductAttributeValueDto;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductAttributeValueMapper;
import ru.stroy1click.product.entity.ProductAttributeValue;
import ru.stroy1click.product.repository.ProductAttributeValueRepository;
import ru.stroy1click.product.service.product.ProductAttributeValueService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {

    private final ProductAttributeValueRepository productAttributeValueRepository;

    private final MessageSource messageSource;

    private final ProductAttributeValueMapper mapper;

    private final CacheClear cacheClear;

    @Override
    @Cacheable(cacheNames = "productAttributeValue", key = "#id")
    public ProductAttributeValueDto get(Integer id) {
        log.info("get {}", id);
         return this.mapper.toDto(this.productAttributeValueRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_type_attribute_value.not_found",
                                null,
                                Locale.getDefault()
                        )
                )
        ));
    }

    @Override
    @Cacheable(cacheNames = "allProductAttributeValuesByProductId", key = "#id")
    public List<ProductAttributeValueDto> getAllByProductId(Integer id) {
        log.info("getAllByProductId {}", id);
         return this.productAttributeValueRepository.findByProduct_Id(id).stream()
                 .map(this.mapper::toDto)
                 .toList();
    }

    @Override
    public Optional<ProductAttributeValue> getByValue(String value) {
        log.info("getByValue {}", value);
        return this.productAttributeValueRepository.findByValue(value);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "allProductAttributeValuesByProductId", key = "#productTypeAttributeValueDto.productId")
    public void create(ProductAttributeValueDto productTypeAttributeValueDto) {
        log.info("create {}", productTypeAttributeValueDto);
        this.productAttributeValueRepository.save(
                this.mapper.toEntity(productTypeAttributeValueDto)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "productAttributeValue", key = "#id")
    public void delete(Integer id) {
        log.info("delete {}", id);
        ProductAttributeValue productAttributeValue = this.productAttributeValueRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_type_attribute_value.not_found",
                                null,
                                Locale.getDefault()
                        )
                )
        );

        this.cacheClear.clearAllProductAttributeValuesByProductId(productAttributeValue.getProduct().getId());
        this.productAttributeValueRepository.delete(productAttributeValue);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productAttributeValue", key = "#id"),
            @CacheEvict(value = "allProductAttributeValuesByProductId", key = "#productAttributeValueDto.productId")
    })
    public void update(Integer id, ProductAttributeValueDto productAttributeValueDto) {
        this.productAttributeValueRepository.findById(id).ifPresentOrElse(productAttributeValue -> {
            ProductAttributeValue updatedProductTypeAttributeValue = ProductAttributeValue.builder()
                    .id(id)
                    .value(productAttributeValueDto.getValue())
                    .product(productAttributeValue.getProduct())
                    .attribute(productAttributeValue.getAttribute())
                    .build();
            this.productAttributeValueRepository.save(updatedProductTypeAttributeValue);
        }, () -> {
            throw new NotFoundException(
                    this.messageSource.getMessage(
                            "error.product_type_attribute_value.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
        });
    }
}