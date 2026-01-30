package ru.stroy1click.catalog.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stroy1click.catalog.service.storage.StorageService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Storage Controller", description = "Работа с S3")
@RequestMapping("/api/v1/storage")
@RateLimiter(name = "storageLimiter")
public class StorageController {

    private final StorageService storageService;

    @GetMapping("/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileName) {
        byte[] data = this.storageService.downloadImage(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_JPEG)
                .body(data);
    }
}
