package ru.stroy1click.product.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import ru.stroy1click.product.dto.ProductAttributeValueDto;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductAttributeValueTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void get_ShouldReturnProductAttributeValue_WhenValueExists() {
        ResponseEntity<ProductAttributeValueDto> response = this.testRestTemplate.getForEntity("/api/v1/product-attribute-values/1", ProductAttributeValueDto.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Red", response.getBody().getValue());
    }

    @Test
    public void create_ShouldCreateProductAttributeValue_WhenDtoIsValid() {
        ProductAttributeValueDto dto = new ProductAttributeValueDto(null, 1, 1, "Blue");
        ResponseEntity<String> response = this.testRestTemplate.postForEntity("/api/v1/product-attribute-values", dto, String.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Значение атрибута продукта создано", response.getBody());
    }

    @Test
    public void update_ShouldUpdateProductAttributeValue_WhenDtoIsValid() {
        ProductAttributeValueDto dto = new ProductAttributeValueDto(null, 1, 1, "Green");
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                "/api/v1/product-attribute-values/2",
                HttpMethod.PATCH,
                new HttpEntity<>(dto),
                String.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Значение атрибута продукта обновлено", response.getBody());

        ResponseEntity<ProductAttributeValueDto> getResponse = this.testRestTemplate.getForEntity("/api/v1/product-attribute-values/2", ProductAttributeValueDto.class);
        Assertions.assertEquals("Green", getResponse.getBody().getValue());
    }

    @Test
    public void delete_ShouldDeleteProductAttributeValue_WhenValueExists() {
        ResponseEntity<String> response = this.testRestTemplate.exchange(
                "/api/v1/product-attribute-values/4",
                HttpMethod.DELETE,
                null,
                String.class
        );

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Значение атрибута продукта удалено", response.getBody());
    }

    @Test
    public void createValidation_ShouldReturnError_WhenDtoIsInvalid() {

        ProductAttributeValueDto invalidDto = new ProductAttributeValueDto(null, 1, 1, "");
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.postForEntity("/api/v1/product-attribute-values", invalidDto, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void updateValidation_ShouldReturnError_WhenDtoIsInvalid() {
        ProductAttributeValueDto invalidDto = new ProductAttributeValueDto(null, 1, 1, "x");
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.exchange(
                "/api/v1/product-attribute-values/1",
                HttpMethod.PATCH,
                new HttpEntity<>(invalidDto),
                ProblemDetail.class
        );

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void get_ShouldReturnNotFound_WhenIdDoesNotExist() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.getForEntity("/api/v1/product-attribute-values/1000000", ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Значение атрибута типа продукта не найдено", response.getBody().getDetail());
    }
}
