package ru.stroy1click.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTypeAttributeValueDto implements Serializable {

    private final static Long SerialVersionUID= 1L;

    private Integer id;

    @NotNull(message = "{validation.product_type_attribute_value_dto.attribute_id.not_null}")
    @Positive(message = "{validation.product_type_attribute_value_dto.attribute_id.min}")
    private Integer attributeId;

    @NotNull(message = "{validation.product_type_attribute_value_dto.product_type_id.not_null}")
    @Positive(message = "{validation.product_type_attribute_value_dto.product_type_id.min}")
    private Integer productTypeId;

    @NotBlank(message = "{validation.product_type_attribute_value_dto.value.not_blank}")
    @Length(min = 2, max = 40, message = "{validation.product_type_attribute_value_dto.value.length}")
    private String value;
}
