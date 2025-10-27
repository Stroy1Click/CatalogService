package ru.stroy1click.product.service.product.type.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stroy1click.product.cache.CacheClear;
import ru.stroy1click.product.dto.ProductTypeAttributeValueDto;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.ProductTypeAttributeValueMapper;
import ru.stroy1click.product.entity.ProductTypeAttributeValue;
import ru.stroy1click.product.repository.ProductTypeAttributeValueRepository;
import ru.stroy1click.product.service.product.type.ProductTypeAttributeValueService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeAttributeValueServiceImpl implements ProductTypeAttributeValueService {

    private final ProductTypeAttributeValueRepository productTypeAttributeValueRepository;

    private final MessageSource messageSource;

    private final ProductTypeAttributeValueMapper mapper;

    private final CacheClear cacheClear;

    @Override
    @Cacheable(cacheNames = "productTypeAttributeValue", key = "#id")
    public ProductTypeAttributeValueDto get(Integer id) {
        log.info("get {}", id);
         return this.mapper.toDto(this.productTypeAttributeValueRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_attribute_value.not_found",
                                null,
                                Locale.getDefault()
                        )
                )
        ));
    }

    @Override
    @Cacheable(cacheNames = "allProductAttributeValuesByProductTypeId", key = "#id")
    public List<ProductTypeAttributeValueDto> getAllByProductId(Integer id) {
        log.info("getAllByProductId {}", id);
         return this.productTypeAttributeValueRepository.findByProductType_Id(id).stream()
                 .map(this.mapper::toDto)
                 .toList();
    }

    @Override
    public Optional<ProductTypeAttributeValue> getByValue(String value) {
        log.info("getByValue {}", value);
        return this.productTypeAttributeValueRepository.findByValue(value);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "allProductAttributeValuesByProductTypeId", key = "#productTypeAttributeValueDto.productTypeId")
    public void create(ProductTypeAttributeValueDto productTypeAttributeValueDto) {
        log.info("create {}", productTypeAttributeValueDto);
        this.productTypeAttributeValueRepository.save(
                this.mapper.toEntity(productTypeAttributeValueDto)
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "productTypeAttributeValue", key = "#id")
    public void delete(Integer id) {
        log.info("delete {}", id);
        ProductTypeAttributeValue productTypeAttributeValue = this.productTypeAttributeValueRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                        this.messageSource.getMessage(
                                "error.product_attribute_value.not_found",
                                null,
                                Locale.getDefault()
                        )
                )
        );

        this.cacheClear.clearAllProductTypeAttributeValuesByProductTypeId(productTypeAttributeValue.getProductType().getId());
        this.productTypeAttributeValueRepository.delete(productTypeAttributeValue);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productTypeAttributeValue", key = "#id"),
            @CacheEvict(value = "allProductAttributeValuesByProductTypeId", key = "#productTypeAttributeValueDto.productTypeId")
    })
    public void update(Integer id, ProductTypeAttributeValueDto productTypeAttributeValueDto) {
        log.info("update {}, {}", id, productTypeAttributeValueDto);
        this.productTypeAttributeValueRepository.findById(id).ifPresentOrElse(productAttributeValue -> {
            ProductTypeAttributeValue updatedProductTypeAttributeValue = ProductTypeAttributeValue.builder()
                    .id(id)
                    .value(productTypeAttributeValueDto.getValue())
                    .productType(productAttributeValue.getProductType())
                    .attribute(productAttributeValue.getAttribute())
                    .build();
            this.productTypeAttributeValueRepository.save(updatedProductTypeAttributeValue);
        }, () -> {
            throw new NotFoundException(
                    this.messageSource.getMessage(
                            "error.product_attribute_value.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
        });
    }
}