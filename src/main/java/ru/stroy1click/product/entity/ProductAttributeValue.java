package ru.stroy1click.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(schema = "product", name = "product_attribute_values")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String value;

    private Integer attributeId;

    private Integer productId;
}