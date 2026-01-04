package ru.stroy1click.product.service.subcategory.impl;

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
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.dto.SubcategoryDto;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.mapper.SubcategoryMapper;
import ru.stroy1click.product.entity.Subcategory;
import ru.stroy1click.product.model.MessageType;
import ru.stroy1click.product.repository.SubcategoryRepository;
import ru.stroy1click.product.service.category.CategoryService;
import ru.stroy1click.product.service.outbox.OutboxMessageService;
import ru.stroy1click.product.service.storage.StorageService;
import ru.stroy1click.product.service.subcategory.SubcategoryService;

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

    private final CacheClear cacheClear;

    private final MessageSource messageSource;

    private final StorageService storageService;

    private final CategoryService categoryService;

    private final OutboxMessageService outboxMessageService;

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
    @Transactional
    @CacheEvict(value = "subcategoriesOfCategory", key = "#subcategoryDto.categoryId")
    public void create(SubcategoryDto subcategoryDto) {
        log.info("create {}", subcategoryDto);

        this.categoryService.get(subcategoryDto.getCategoryId());

        Subcategory createdSubcategory = this.subcategoryRepository.save(this.subcategoryMapper.toEntity(subcategoryDto));
        this.outboxMessageService.save(this.subcategoryMapper.toDto(createdSubcategory), MessageType.SUBCATEGORY_CREATED);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "subcategory", key = "#id"),
            @CacheEvict(value = "subcategoriesOfCategory", key = "#subcategoryDto.categoryId")
    })
    public void update(Integer id, SubcategoryDto subcategoryDto) {
        log.info("update {}, {}", id, subcategoryDto);

        this.subcategoryRepository.findById(id).ifPresentOrElse(subcategory -> {
            Subcategory updatedSubcategory = Subcategory.builder()
                    .id(id)
                    .title(subcategoryDto.getTitle())
                    .category(subcategory.getCategory())
                    .products(subcategory.getProducts())
                    .build();

            this.subcategoryRepository.save(updatedSubcategory);
            this.outboxMessageService.save(this.subcategoryMapper.toDto(updatedSubcategory), MessageType.SUBCATEGORY_UPDATED);
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
            @CacheEvict(value = "clearPaginationOfProductsBySubcategory", key = "#id")
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

        this.subcategoryRepository.deleteById(id);
        this.outboxMessageService.save(id, MessageType.SUBCATEGORY_DELETED);
        this.cacheClear.clearSubcategoriesOfCategory(subcategory.getCategory().getId());
    }

    @Override
    public Optional<Subcategory> getByTitle(String title) {
        log.info("getByTitle {}", title);
        return this.subcategoryRepository.findByTitle(title);
    }

    @Override
    @Cacheable(value = "subcategoriesOfCategory", key = "#categoryId")
    public List<SubcategoryDto> getByCategoryId(Integer categoryId) {
        log.info("getByCategoryId {}", categoryId);
        return this.subcategoryRepository.findByCategory_Id(categoryId)
                .stream()
                .map(this.subcategoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "subcategory", key = "#id")
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
    @CacheEvict(value = "subcategory", key = "#id")
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