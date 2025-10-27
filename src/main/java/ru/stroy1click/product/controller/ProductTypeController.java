package ru.stroy1click.product.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.ProductTypeDto;
import ru.stroy1click.product.exception.ValidationException;
import ru.stroy1click.product.service.product.type.ProductTypeService;
import ru.stroy1click.product.util.ImageValidatorUtils;
import ru.stroy1click.product.util.ValidationErrorUtils;
import ru.stroy1click.product.validator.product.type.ProductTypeCreateValidator;
import ru.stroy1click.product.validator.product.type.ProductTypeUpdateValidator;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-types")
@Tag(name = "ProductType Controller", description = "Взаимодействие с типом продукта")
@RateLimiter(name = "productTypeLimiter")
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    private final ProductTypeCreateValidator createValidator;

    private final ProductTypeUpdateValidator updateValidator;

    private final MessageSource messageSource;

    private final ImageValidatorUtils imageValidator;

    @GetMapping("/{id}")
    @Operation(summary = "Получить тип продукта")
    public ProductTypeDto get(@PathVariable("id") Integer id){
        return this.productTypeService.get(id);
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "Загрузить изображение типа продукта")
    public ResponseEntity<String> assignImage(@PathVariable("id") Integer id,
                                              @RequestParam("image") MultipartFile image){
        this.imageValidator.validateImage(image);

        this.productTypeService.assignImage(id, image);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type.image.upload",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Удалить изображение типа продукта")
    public ResponseEntity<String> deleteImage(@PathVariable("id") Integer id,
                                              @RequestParam("link") String link){
        this.productTypeService.deleteImage(id, link);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type.image.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }


    @GetMapping
    @Operation(summary = "Получить типы продуктов по подкатегории")
    public List<ProductTypeDto> getBySubcategory(@RequestParam("subcategoryId") Integer subcategoryId){
        return this.productTypeService.getBySubcategory(subcategoryId);
    }

    @PostMapping
    @Operation(summary = "Создать тип продукта")
    public ResponseEntity<String> create(@RequestBody @Valid ProductTypeDto productTypeDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        this.createValidator.validate(productTypeDto);

        this.productTypeService.create(productTypeDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type.create",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновить тип продукта")
    public ResponseEntity<String> update(@PathVariable("id") Integer id,
                                         @RequestBody @Valid ProductTypeDto productTypeDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        productTypeDto.setId(id); //for update validation
        this.updateValidator.validate(productTypeDto);

        this.productTypeService.update(id, productTypeDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type.update",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип продукта")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id){
        this.productTypeService.delete(id);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }
}