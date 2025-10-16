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
import ru.stroy1click.product.dto.ProductTypeAttributeValueDto;
import ru.stroy1click.product.exception.ValidationException;
import ru.stroy1click.product.service.product.type.ProductTypeAttributeValueService;
import ru.stroy1click.product.util.ValidationErrorUtils;
import ru.stroy1click.product.validator.product.ProductAttributeValueCreateValidator;
import ru.stroy1click.product.validator.product.ProductAttributeValueUpdateValidator;
import ru.stroy1click.product.validator.product.type.ProductTypeAttributeValueCreateValidator;
import ru.stroy1click.product.validator.product.type.ProductTypeAttributeValueUpdateValidator;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-type-attribute-values")
@Tag(name = "Product Type Attribute Controller", description = "Взаимодействие со значениями атрибута типа продукта")
@RateLimiter(name = "productTypeAttributeLimiter")
public class ProductTypeAttributeValueController {

    private final ProductTypeAttributeValueService productTypeAttributeValueService;

    private final ProductTypeAttributeValueCreateValidator createValidator;

    private final ProductTypeAttributeValueUpdateValidator updateValidator;

    private final MessageSource messageSource;

    @GetMapping("/{id}")
    @Operation(summary = "Получение значения атрибута типа продукта")
    public ResponseEntity<ProductTypeAttributeValueDto> get(@PathVariable("id") Integer id){
        return ResponseEntity.ok(this.productTypeAttributeValueService.get(id));
    }

    @PostMapping
    @Operation(summary = "Создание значения атрибута типа продукта")
    public ResponseEntity<String> create(@RequestBody @Valid ProductTypeAttributeValueDto productTypeAttributeValueDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        this.createValidator.validate(productTypeAttributeValueDto);

        this.productTypeAttributeValueService.create(productTypeAttributeValueDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type_attribute_value.create",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновление значения атрибута типа продукта")
    public ResponseEntity<String> update(@PathVariable("id") Integer id,
                                         @RequestBody @Valid ProductTypeAttributeValueDto productTypeAttributeValueDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        productTypeAttributeValueDto.setId(id); //for update validation
        this.updateValidator.validate(productTypeAttributeValueDto);

        this.productTypeAttributeValueService.update(id, productTypeAttributeValueDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type_attribute_value.update",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление значения атрибута типа продукта")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id){
        this.productTypeAttributeValueService.delete(id);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_type_attribute_value.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }
}