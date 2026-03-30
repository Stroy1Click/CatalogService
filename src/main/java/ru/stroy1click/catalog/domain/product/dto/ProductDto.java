package ru.stroy1click.catalog.domain.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.stroy1click.common.dto.Unit;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto implements Serializable {

    private final static Long SerialVersionUID= 1L;

    private Integer id;

    @NotBlank(message = "{validation.productdto.title.not_blank}")
    @Length(min = 2, max = 60, message = "{validation.productdto.title.length}")
    private String title;

    @NotBlank(message = "{validation.productdto.description.not_blank}")
    @Length(max = 1000, message = "{validation.productdto.description.length}")
    private String description;

    @NotNull(message = "{validation.productdto.price.not_null}")
    @DecimalMin(value = "1", message = "{validation.productdto.price.min}")
    private BigDecimal price;

    @NotNull(message = "{validation.productdto.unit.not_null}")
    private Unit unit;

    @NotNull(message = "{validation.productdto.in_stock.not_null}")
    private Boolean inStock;

    @NotNull(message = "{validation.productdto.category_id.not_null}")
    @Min(value = 1, message = "{validation.productdto.category_id.min}")
    private Integer categoryId;

    @NotNull(message = "{validation.productdto.subcategory_id.not_null}")
    @Min(value = 1, message = "{validation.productdto.subcategory_id.min}")
    private Integer subcategoryId;

    @NotNull(message = "{validation.productdto.product_type_id.not_null}")
    @Min(value = 1, message = "{validation.productdto.product_type_id.min}")
    private Integer productTypeId;
}
