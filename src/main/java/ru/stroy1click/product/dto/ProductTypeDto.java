package ru.stroy1click.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeDto implements Serializable {

    private final static Long SerialVersionUID= 1L;

    private Integer id;

    @NotNull(message = "{validation.product_type_dto.subcategory_id.not_null}")
    @Min(value = 1, message = "{validation.product_type_dto.subcategory_id.min}")
    private Integer subcategoryId;

    private String image;

    @NotBlank(message = "{validation.product_type_dto.title.not_blank}")
    @Length(min = 2, max = 40, message = "{validation.product_type_dto.title.length}")
    private String title;
}
