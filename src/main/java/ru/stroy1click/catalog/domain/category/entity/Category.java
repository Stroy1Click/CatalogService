package ru.stroy1click.catalog.domain.category.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.catalog.domain.subcategory.entity.Subcategory;
import ru.stroy1click.catalog.domain.product.entity.Product;

import java.util.List;

@Data
@Table(schema = "catalog", name = "categories")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String image;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Subcategory> subcategories;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}
