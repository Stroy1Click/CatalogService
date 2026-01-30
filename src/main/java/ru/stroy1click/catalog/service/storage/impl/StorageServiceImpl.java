package ru.stroy1click.catalog.service.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.exception.StorageException;
import ru.stroy1click.catalog.prop.StorageProperties;
import ru.stroy1click.catalog.service.storage.StorageService;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private final StorageProperties storageProperties;

    private final S3Client s3Client;

    private final MessageSource messageSource;

    @Override
    public String uploadImage(MultipartFile image) {
        log.info("uploadImage");
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        try {
            this.s3Client.putObject(PutObjectRequest.builder()
                            .bucket(this.storageProperties.getBucketName())
                            .key(fileName)
                            .build(),
                    RequestBody.fromBytes(image.getBytes()));
        } catch (IOException e) {
            log.error("uploadImage error ", e);
            throwStorageException(e);
        }
        return fileName;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> imageList) {
        log.info("uploadImages");
        List<String> fileNameList = new ArrayList<>();
        for(MultipartFile file : imageList){
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            try {
                this.s3Client.putObject(PutObjectRequest.builder()
                                .bucket(this.storageProperties.getBucketName())
                                .key(fileName)
                                .build(),
                        RequestBody.fromBytes(file.getBytes()));
            } catch (IOException e) {
                log.error("uploadImages error ", e);
                throwStorageException(e);
            }
            fileNameList.add(fileName);
        }
        return fileNameList;
    }

    @Override
    @Cacheable(value = "image", key = "#fileName")
    public byte[] downloadImage(String fileName) {
        log.info("downloadFile {}", fileName);
        ResponseBytes<GetObjectResponse> objectAsBytes =
                this.s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(this.storageProperties.getBucketName())
                        .key(fileName)
                        .build());
        return objectAsBytes.asByteArray();
    }

    @Override
    @CacheEvict(value = "image", key = "#fileName")
    public void deleteImage(String fileName) {
        log.info("deleteFile {}", fileName);
        this.s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(this.storageProperties.getBucketName())
                .key(fileName)
                .build());
    }

    private void throwStorageException(IOException e){
        log.error("uploadImages exception {}", e.getMessage());
        throw new StorageException(
                this.messageSource.getMessage(
                        "error.storage.upload",
                        null,
                        Locale.getDefault()
                )
        );
    }
}
