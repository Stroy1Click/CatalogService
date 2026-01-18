package ru.stroy1click.product.cache;

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

    public void clearPaginationOfProductsByCategory(Integer categoryId){
        log.info("clearPaginationOfProductsByCategory {}", categoryId);
        deleteCache("clearPaginationOfProductsByCategory", categoryId); //TODO ERROR!
    }

    public void clearPaginationOfProductsBySubcategory(Integer subcategoryId){
        log.info("clearPaginationOfProductsBySubcategory {}", subcategoryId);
        deleteCache("clearPaginationOfProductsBySubcategory", subcategoryId);
    }

    public void clearPaginationOfProductsByProductType(Integer productTypeId){
        log.info("clearPaginationOfProductsByProductType {}", productTypeId);
        deleteCache("clearPaginationOfProductsByProductType", productTypeId);
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