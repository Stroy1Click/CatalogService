package ru.stroy1click.catalog.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic categoryCreatedEventsTopic(){
        return TopicBuilder.name("category-created-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic categoryUpdatedEventsTopic(){
        return TopicBuilder.name("category-updated-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic categoryDeletedEventsTopic(){
        return TopicBuilder.name("category-deleted-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic subcategoryCreatedEventsTopic(){
        return TopicBuilder.name("subcategory-created-events")
                .replicas(1)
                .partitions(3)
                .build();
    }


    @Bean
    public NewTopic subcategoryUpdatedEventsTopic(){
        return TopicBuilder.name("subcategory-updated-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic subcategoryDeletedEventsTopic(){
        return TopicBuilder.name("subcategory-deleted-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic productTypeCreatedEventsTopic(){
        return TopicBuilder.name("product-type-created-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic productTypeUpdatedEventsTopic(){
        return TopicBuilder.name("product-type-updated-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic productTypeDeletedEventsTopic(){
        return TopicBuilder.name("product-type-deleted-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic productCreatedEventsTopic(){
        return TopicBuilder.name("product-created-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic productUpdatedEventsTopic(){
        return TopicBuilder.name("product-updated-events")
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    public NewTopic productDeletedEventsTopic(){
        return TopicBuilder.name("product-deleted-events")
                .replicas(1)
                .partitions(3)
                .build();
    }
}
