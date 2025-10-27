package ru.stroy1click.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Подключение к локальному Redis
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setDatabase(0);

        return Redisson.create(config);
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        long oneDayMillis = 24 * 60 * 60 * 1000L; // 1 день

        config.put("category", new CacheConfig(oneDayMillis, 0));
        config.put("allCategories", new CacheConfig(oneDayMillis, 0));
        config.put("subcategory", new CacheConfig(oneDayMillis, 0));
        config.put("subcategoriesOfCategory", new CacheConfig(oneDayMillis, 0));
        config.put("product", new CacheConfig(oneDayMillis, 0));
        config.put("productImages", new CacheConfig(oneDayMillis, 0));
        config.put("productAttributeValue", new CacheConfig(oneDayMillis, 0));
        config.put("allProductAttributeValuesByProductId", new CacheConfig(oneDayMillis, 0));
        config.put("clearPaginationOfProductsByCategory", new CacheConfig(oneDayMillis, 0));
        config.put("clearPaginationOfProductsBySubcategory", new CacheConfig(oneDayMillis, 0));
        config.put("productType", new CacheConfig(oneDayMillis, 0));
        config.put("productTypesOfSubcategory", new CacheConfig(oneDayMillis, 0));
        config.put("attribute", new CacheConfig(oneDayMillis, 0));
        config.put("allAttributes", new CacheConfig(oneDayMillis, 0));
        config.put("attributesByProductType", new CacheConfig(oneDayMillis, 0));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
