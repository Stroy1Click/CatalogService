package ru.stroy1click.catalog.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.stroy1click.catalog.domain.category.entity.Category;
import ru.stroy1click.catalog.domain.subcategory.entity.Subcategory;
import ru.stroy1click.catalog.domain.producttype.entity.ProductType;
import ru.stroy1click.common.dto.Unit;

import java.math.BigDecimal;

@Data
@Table(schema = "catalog", name = "products")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String description;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    private Boolean inStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", referencedColumnName = "id")
    private Subcategory subcategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductType productType;
}
