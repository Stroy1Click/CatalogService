package ru.stroy1click.catalog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Table(schema = "catalog", name = "subcategories")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Subcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String image;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @OneToMany(mappedBy = "subcategory")
    private List<ProductType> productTypes;

    @OneToMany(mappedBy = "subcategory")
    private List<Product> products;
}
