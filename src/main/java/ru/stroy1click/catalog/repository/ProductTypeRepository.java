package ru.stroy1click.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.entity.ProductType;

import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Integer>{

    Optional<ProductType> findByTitle(String title);
}
