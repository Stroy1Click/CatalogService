package ru.stroy1click.catalog.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.dto.ProductImageDto;
import ru.stroy1click.catalog.exception.ValidationException;
import ru.stroy1click.catalog.dto.PageResponse;
import ru.stroy1click.catalog.service.product.ProductImageService;
import ru.stroy1click.catalog.service.product.ProductPaginationService;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.util.ImageValidatorUtils;
import ru.stroy1click.catalog.util.ValidationErrorUtils;
import ru.stroy1click.catalog.validator.product.ProductCreateValidator;
import ru.stroy1click.catalog.validator.product.ProductUpdateValidator;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Product Controller", description = "Взаимодействие с продуктами")
@RateLimiter(name = "productLimiter")
public class ProductController {

    private final ProductService productService;

    private final ProductCreateValidator createValidator;

    private final ProductUpdateValidator updateValidator;

    private final ProductPaginationService productPaginationService;

    private final MessageSource messageSource;

    private final ProductImageService productImageService;

    private final ImageValidatorUtils imageValidator;

    @GetMapping("/{id}")
    @Operation(summary = "Получить продукт")
    public ProductDto get(@PathVariable("id") Integer id){
        return this.productService.get(id);
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "Получить изображения продукта")
    public List<ProductImageDto> getImages(@PathVariable("id") Integer id){
        return this.productImageService.getAllByProductId(id);
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Загрузить изображения продукту")
    public ResponseEntity<String> assignImages(@PathVariable("id") Integer id,
                                               @RequestParam("images") List<MultipartFile> images){
        this.imageValidator.validateImages(images);

        this.productService.assignImages(id, images);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product.images.upload",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}/images")
    @Operation(summary = "Удалить изображения продукта")
    public ResponseEntity<String> deleteImage(@PathVariable("id") Integer id,
                                              @RequestParam("link") String link){
        this.productService.deleteImage(id,link);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product.image.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @GetMapping
    @Operation(summary = "Получить продукты с пагинацией")
    public PageResponse<ProductDto> getProductsByPagination(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "subcategoryId", required = false) Integer subcategoryId,
            @RequestParam(value = "productTypeId", required = false) Integer productType
    ) {
        return this.productPaginationService.getProducts(categoryId, subcategoryId, productType, PageRequest.of(page, size));
    }


    @PostMapping
    @Operation(summary = "Создать продукт")
    public ResponseEntity<ProductDto> create(@RequestBody @Valid ProductDto productDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        this.createValidator.validate(productDto);

        ProductDto createdProduct = this.productService.create(productDto);

        return ResponseEntity
                .created(URI.create("/api/v1/products/" + createdProduct.getId()))
                .body(createdProduct);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновить продукт")
    public ResponseEntity<String> update(@PathVariable("id") Integer id,
                                         @RequestBody @Valid ProductDto productDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        productDto.setId(id); //for update validation
        this.updateValidator.validate(productDto);

        this.productService.update(id, productDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product.update",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить продукт")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id){
        this.productService.delete(id);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }
}