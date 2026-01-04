package ru.stroy1click.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class Stroy1ClickProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Stroy1ClickProductServiceApplication.class, args);
    }

}
