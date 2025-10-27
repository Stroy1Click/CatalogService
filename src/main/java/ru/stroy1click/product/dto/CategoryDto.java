package ru.stroy1click.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto implements Serializable {

    private final static Long SerialVersionUID= 1L;

    private Integer id;

    private String image;

    @NotBlank(message = "{validation.categorydto.title.not_blank}")
    @Length(min = 2, max = 40, message = "{validation.categorydto.title.length}")
    private String title;
}