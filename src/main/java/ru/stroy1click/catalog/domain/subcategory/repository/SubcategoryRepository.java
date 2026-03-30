package ru.stroy1click.catalog.domain.subcategory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.domain.subcategory.entity.Subcategory;

import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Integer>{

    Optional<Subcategory> findByTitle(String title);
}
