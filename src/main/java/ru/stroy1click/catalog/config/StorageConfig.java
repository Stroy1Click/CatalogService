package ru.stroy1click.catalog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stroy1click.catalog.prop.StorageProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageProperties storageProperties;

    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(this.storageProperties.getAccessKey()
                ,this.storageProperties.getSecretKey());

        return S3Client.builder()
                .region(Region.of(this.storageProperties.getRegion()))
                .endpointOverride(URI.create("https://s3.ru-7.storage.selcloud.ru"))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }
}
