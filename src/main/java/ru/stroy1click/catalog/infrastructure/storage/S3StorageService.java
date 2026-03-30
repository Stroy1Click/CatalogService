package ru.stroy1click.catalog.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.domain.common.service.StorageService;
import ru.stroy1click.catalog.infrastructure.prop.StorageProperties;
import ru.stroy1click.common.exception.StorageException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final StorageProperties storageProperties;

    private final S3Client s3Client;

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
        } catch (S3Exception e) {
            log.error("S3 Service Error: [Code: {}] {}", e.awsErrorDetails().errorCode(), e.getMessage());
            throw new StorageException(e);
        } catch (Exception e) {
            log.error("Unexpected error during document upload to S3", e);
            throw new StorageException(e);
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
            } catch (S3Exception e) {
                log.error("S3 Service Error: [Code: {}] {}", e.awsErrorDetails().errorCode(), e.getMessage());
                throw new StorageException(e);
            } catch (Exception e) {
                log.error("Unexpected error during document upload to S3", e);
                throw new StorageException(e);
            }
            fileNameList.add(fileName);
        }
        return fileNameList;
    }

    @Override
    @Cacheable(value = "image", key = "#fileName")
    public byte[] downloadImage(String fileName) {
        log.info("downloadFile {}", fileName);
        try {
            ResponseBytes<GetObjectResponse> objectAsBytes =
                    this.s3Client.getObjectAsBytes(GetObjectRequest.builder()
                            .bucket(this.storageProperties.getBucketName())
                            .key(fileName)
                            .build());

            return objectAsBytes.asByteArray();
        } catch (S3Exception e) {
            log.error("S3 Service Error: [Code: {}] {}", e.awsErrorDetails().errorCode(), e.getMessage());
            throw new StorageException(e);
        } catch (Exception e) {
            log.error("Unexpected error during document upload to S3", e);
            throw new StorageException(e);
        }
    }

    @Override
    @CacheEvict(value = "image", key = "#fileName")
    public void deleteImage(String fileName) {
        log.info("deleteFile {}", fileName);
        try {
            this.s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(this.storageProperties.getBucketName())
                    .key(fileName)
                    .build());
        } catch (S3Exception e) {
            log.error("S3 Service Error: [Code: {}] {}", e.awsErrorDetails().errorCode(), e.getMessage());
            throw new StorageException(e);
        } catch (Exception e) {
            log.error("Unexpected error during document upload to S3", e);
            throw new StorageException(e);
        }
    }
}
