package ru.stroy1click.product;

import org.springframework.boot.SpringApplication;

public class TestStroy1ClickProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(Stroy1ClickProductServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
