package ru.stroy1click.catalog.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClear {

    private final CacheManager cacheManager;

    public void clearSubcategoriesOfCategory(Integer categoryId){
        log.info("clearSubcategoriesOfCertainCategory {}", categoryId);
        deleteCache("subcategoriesOfCategory",categoryId);
    }

    public void clearProductTypesOfSubcategory(Integer subcategoryId) {
        log.info("clearProductsTypesOfCertainSubcategory {}", subcategoryId);
        deleteCache("productTypesBySubcategory",subcategoryId);
    }

    public void clearProductImages(Integer productId){
        log.info("clearProductImages {productId}");
        deleteCache("productImages", productId);
    }

    private void deleteCache(String key, Integer value){
        Cache cache = this.cacheManager.getCache(key);
        if(cache != null){
            cache.evict(value);
        }
    }
}