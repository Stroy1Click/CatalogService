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
import ru.stroy1click.product.dto.SubcategoryDto;
import ru.stroy1click.product.exception.ValidationException;
import ru.stroy1click.product.service.subcategory.SubcategoryService;
import ru.stroy1click.product.util.ImageValidatorUtils;
import ru.stroy1click.product.util.ValidationErrorUtils;
import ru.stroy1click.product.validator.subcategory.SubcategoryCreateValidator;
import ru.stroy1click.product.validator.subcategory.SubcategoryUpdateValidator;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subcategories")
@Tag(name = "Subcategory Controller", description = "Взаимодействие с подкатегориями")
@RateLimiter(name = "subcategoryLimiter")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    private final SubcategoryCreateValidator createValidator;

    private final SubcategoryUpdateValidator updateValidator;

    private final MessageSource messageSource;

    private final ImageValidatorUtils imageValidator;

    @GetMapping("/{id}")
    @Operation(summary = "Получить подкатегорию")
    public SubcategoryDto get(@PathVariable("id") Integer id){
        return this.subcategoryService.get(id);
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Получить все подкатегории, которые принадлежат категории {id}")
    public List<SubcategoryDto> getSubcategories(@PathVariable("id") Integer id){
        return this.subcategoryService.getByCategoryId(id);
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "Загрузить изображение подкатегории")
    public ResponseEntity<String> assignImage(@PathVariable("id") Integer id,
                                              @RequestParam("image") MultipartFile image){
        this.imageValidator.validateImage(image);

        this.subcategoryService.assignImage(id, image);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.subcategory.image.upload",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Удалить изображение подкатегории")
    public ResponseEntity<String> deleteImage(@PathVariable("id") Integer id,
                                              @RequestParam("link") String link){
        this.subcategoryService.deleteImage(id, link);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.subcategory.image.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PostMapping
    @Operation(summary = "Создать подкатегорию")
    public ResponseEntity<String> create(@RequestBody @Valid SubcategoryDto subcategoryDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        this.createValidator.validate(subcategoryDto);

        this.subcategoryService.create(subcategoryDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.subcategory.create",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновить подкатегорию")
    public ResponseEntity<String> update(@PathVariable("id") Integer id,
                                         @RequestBody @Valid SubcategoryDto subcategoryDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        subcategoryDto.setId(id); //for update validation
        this.updateValidator.validate(subcategoryDto);

        this.subcategoryService.update(id, subcategoryDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.subcategory.update",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить подкатегорию")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id){
        this.subcategoryService.delete(id);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.subcategory.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }
}
