package ru.stroy1click.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RedissonConfig {

    @Value("${redisson.host:localhost}")
    private String host;

    @Value("${redisson.port:6379}")
    private Integer port;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://%s:%d".formatted(this.host, this.port))
                .setDatabase(0);

        return Redisson.create(config);
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        long oneDayMillis = 24 * 60 * 60 * 1000L; // 1 день

        config.put("category", new CacheConfig(oneDayMillis, 0));
        config.put("allCategories", new CacheConfig(oneDayMillis, 0));
        config.put("allSubcategories", new CacheConfig(oneDayMillis, 0));
        config.put("allProductTypes", new CacheConfig(oneDayMillis, 0));
        config.put("allProducts", new CacheConfig(oneDayMillis, 0));
        config.put("subcategory", new CacheConfig(oneDayMillis, 0));
        config.put("subcategoriesOfCategory", new CacheConfig(oneDayMillis, 0));
        config.put("product", new CacheConfig(oneDayMillis, 0));
        config.put("productImages", new CacheConfig(oneDayMillis, 0));
        config.put("productType", new CacheConfig(oneDayMillis, 0));
        config.put("productTypesOfSubcategory", new CacheConfig(oneDayMillis, 0));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
