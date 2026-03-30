package ru.stroy1click.catalog.domain.subcategory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.catalog.domain.category.entity.Category;
import ru.stroy1click.catalog.domain.product.entity.Product;
import ru.stroy1click.catalog.domain.producttype.entity.ProductType;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @OneToMany(mappedBy = "subcategory", fetch = FetchType.LAZY)
    private List<ProductType> productTypes;

    @OneToMany(mappedBy = "subcategory", fetch = FetchType.LAZY)
    private List<Product> products;
}
