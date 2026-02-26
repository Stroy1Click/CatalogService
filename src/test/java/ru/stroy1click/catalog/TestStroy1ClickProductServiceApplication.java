package ru.stroy1click.catalog;

import org.springframework.boot.SpringApplication;
import ru.stroy1click.catalog.config.TestcontainersConfiguration;

public class TestStroy1ClickProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickCatalogServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
