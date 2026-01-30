package ru.stroy1click.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.entity.ProductImage;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    Optional<ProductImage> findByLink(String link);

    List<ProductImage> findAllByProduct_Id(Integer productId);
}
