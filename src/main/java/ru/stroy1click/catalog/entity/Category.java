package ru.stroy1click.catalog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "category")
    private List<Subcategory> subcategories;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}
