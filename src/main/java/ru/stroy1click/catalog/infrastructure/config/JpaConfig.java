package ru.stroy1click.catalog.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
        "ru.stroy1click.catalog",
        "ru.stroy1click.outbox.entity"
})
@EnableJpaRepositories(basePackages = {
        "ru.stroy1click.catalog",
        "ru.stroy1click.outbox.repository"
})
public class JpaConfig {

}