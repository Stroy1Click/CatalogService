package ru.stroy1click.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.entity.Product;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByTitle(String title);

    @Query("select p.id from Product p where p.category.id = :categoryId")
    Page<Integer> findProductIdsByCategory_Id(@Param("categoryId") Integer categoryId, Pageable pageable);

    @Query("select p.id from Product p where p.subcategory.id = :subcategoryId")
    Page<Integer> findProductIdsBySubcategory_Id(@Param("subcategoryId") Integer subcategoryId, Pageable pageable);

    @Query("select p.id from Product p where p.productType.id = :productTypeId")
    Page<Integer> findProductIdsByProductType_Id(@Param("productTypeId") Integer productTypeId, Pageable pageable);
}
