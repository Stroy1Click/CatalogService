package ru.stroy1click.catalog.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class StorageProperties {

    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.region}")
    private String region;

    @Value("${s3.bucket-name}")
    private String bucketName;

}
