package ru.stroy1click.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubcategoryDto implements Serializable {

    private final static Long SerialVersionUID= 1L;

    private Integer id;

    @NotNull(message = "{validation.subcategorydto.category_id.not_null}")
    @Min(value = 1, message = "{validation.subcategorydto.category_id.min}")
    private Integer categoryId;

    private String image;

    @NotBlank(message = "{validation.subcategorydto.title.not_blank}")
    @Length(min = 2, max = 40, message = "{validation.subcategorydto.title.length}")
    private String title;
}
