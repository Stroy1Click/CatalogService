package ru.stroy1click.catalog.domain.producttype.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.domain.producttype.entity.ProductType;

import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Integer>{

    Optional<ProductType> findByTitle(String title);
}
