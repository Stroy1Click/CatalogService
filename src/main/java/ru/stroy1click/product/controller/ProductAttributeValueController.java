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
import ru.stroy1click.product.dto.ProductAttributeValueDto;
import ru.stroy1click.product.dto.ProductTypeAttributeValueDto;
import ru.stroy1click.product.exception.ValidationException;
import ru.stroy1click.product.service.product.ProductAttributeValueService;
import ru.stroy1click.product.util.ValidationErrorUtils;
import ru.stroy1click.product.validator.product.ProductAttributeValueCreateValidator;
import ru.stroy1click.product.validator.product.ProductAttributeValueUpdateValidator;
import ru.stroy1click.product.validator.product.ProductCreateValidator;
import ru.stroy1click.product.validator.product.ProductUpdateValidator;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-attribute-values")
@Tag(name = "Product Attribute Value Controller", description = "Взаимодействие со значениями атрибута продукта")
@RateLimiter(name = "productAttributeValueLimiter")
public class ProductAttributeValueController {

    private final ProductAttributeValueService productAttributeValueService;

    private final ProductAttributeValueCreateValidator createValidator;

    private final ProductAttributeValueUpdateValidator updateValidator;

    private final MessageSource messageSource;

    @GetMapping("/{id}")
    @Operation(summary = "Получение значения атрибута продукта")
    public ResponseEntity<ProductAttributeValueDto> get(@PathVariable("id") Integer id){
        return ResponseEntity.ok(this.productAttributeValueService.get(id));
    }

    @PostMapping
    @Operation(summary = "Создание значения атрибута продукта")
    public ResponseEntity<String> create(@RequestBody @Valid ProductAttributeValueDto productAttributeValueDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        this.createValidator.validate(productAttributeValueDto);

        this.productAttributeValueService.create(productAttributeValueDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_attribute_value.create",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновление значения атрибута продукта")
    public ResponseEntity<String> update(@PathVariable("id") Integer id,
                                         @RequestBody @Valid ProductAttributeValueDto productAttributeValueDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        productAttributeValueDto.setId(id); //for update validator
        this.updateValidator.validate(productAttributeValueDto);

        this.productAttributeValueService.update(id, productAttributeValueDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_attribute_value.update",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление значения атрибута продукта")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id){
        this.productAttributeValueService.delete(id);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.product_attribute_value.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }
}
