package ru.stroy1click.product.integration;

import org.springframework.boot.SpringApplication;
import ru.stroy1click.product.Stroy1ClickProductServiceApplication;

public class TestStroy1ClickProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickProductServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
