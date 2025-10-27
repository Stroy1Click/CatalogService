package ru.stroy1click.product.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.product.dto.CategoryDto;
import ru.stroy1click.product.exception.ValidationException;
import ru.stroy1click.product.service.category.CategoryService;
import ru.stroy1click.product.util.ImageValidatorUtils;
import ru.stroy1click.product.util.ValidationErrorUtils;
import ru.stroy1click.product.validator.category.CategoryCreateValidator;
import ru.stroy1click.product.validator.category.CategoryUpdateValidator;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Controller", description = "Взаимодействие с категориями")
@RateLimiter(name = "categoryLimiter")
public class CategoryController {

    private final CategoryService categoryService;

    private final CategoryCreateValidator createValidator;

    private final CategoryUpdateValidator updateValidator;

    private final MessageSource messageSource;

    private final ImageValidatorUtils imageValidator;

    @GetMapping("/{id}")
    @Operation(summary = "Получить категорию")
    public CategoryDto get(@PathVariable("id") Integer id){
        return this.categoryService.get(id);
    }

    @GetMapping
    @Operation(summary = "Получить все категории")
    public List<CategoryDto> getCategories(){
        return this.categoryService.getAll();
    }

    @PostMapping("/{id}/image")
    @Operation(summary = "Загрузить изображение категории")
    public ResponseEntity<String> assignImage(@PathVariable("id") Integer id, @RequestParam("image")
                                              MultipartFile image){
        this.imageValidator.validateImage(image);

        this.categoryService.assignImage(id, image);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.category.image.upload",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}/image")
    @Operation(summary = "Удалить изображение категории")
    public ResponseEntity<String> deleteImage(@PathVariable("id") Integer id,
                                              @RequestParam("link") String link){
        this.categoryService.deleteImage(id, link);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.category.image.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PostMapping
    @Operation(summary = "Создать категорию")
    public ResponseEntity<String> create(@RequestBody @Valid CategoryDto categoryDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        this.createValidator.validate(categoryDto);

        this.categoryService.create(categoryDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.category.create",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновить категорию")
    public ResponseEntity<String> update(@PathVariable("id") Integer id,
                                         @RequestBody @Valid CategoryDto categoryDto,
                                         BindingResult bindingResult){
        if(bindingResult.hasFieldErrors()) throw new ValidationException(ValidationErrorUtils.collectErrorsToString(
                bindingResult.getFieldErrors()
        ));

        categoryDto.setId(id); //for update validation
        this.updateValidator.validate(categoryDto);

        this.categoryService.update(id, categoryDto);
        return ResponseEntity.ok(
                this.messageSource.getMessage(
                        "info.category.update",
                        null,
                        Locale.getDefault()
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить категорию")
    public ResponseEntity<String> delete(@PathVariable("id") Integer id){
        this.categoryService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(
                this.messageSource.getMessage(
                        "info.category.delete",
                        null,
                        Locale.getDefault()
                )
        );
    }
}
