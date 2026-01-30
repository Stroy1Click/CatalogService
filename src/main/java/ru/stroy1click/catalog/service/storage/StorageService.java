package ru.stroy1click.catalog.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    String uploadImage(MultipartFile multipartFile);

    List<String> uploadImages(List<MultipartFile> file);

    byte[] downloadImage(String fileName);

    void deleteImage(String fileName);
}
