package ru.stroy1click.catalog.domain.producttype.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.catalog.domain.product.entity.Product;
import ru.stroy1click.catalog.domain.subcategory.entity.Subcategory;

import java.util.List;

@Data
@Table(schema = "catalog", name = "product_types")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", referencedColumnName = "id")
    private Subcategory subcategory;

    @OneToMany(mappedBy = "productType", fetch = FetchType.LAZY)
    private List<Product> products;
}
