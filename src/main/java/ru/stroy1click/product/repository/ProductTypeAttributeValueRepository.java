package ru.stroy1click.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.product.entity.ProductTypeAttributeValue;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTypeAttributeValueRepository extends JpaRepository<ProductTypeAttributeValue, Integer> {

    List<ProductTypeAttributeValue> findByProductType_Id(Integer productId);

    Optional<ProductTypeAttributeValue> findByValue(String value);
}
