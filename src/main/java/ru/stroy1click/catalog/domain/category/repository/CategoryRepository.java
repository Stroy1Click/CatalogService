package ru.stroy1click.catalog.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stroy1click.catalog.domain.category.entity.Category;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>{

    Optional<Category> findByTitle(String title);

}
