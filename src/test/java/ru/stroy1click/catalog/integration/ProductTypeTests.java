package ru.stroy1click.catalog.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.stroy1click.catalog.dto.ProductTypeDto;

import java.io.IOException;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductTypeTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void get_ShouldReturnProductTypeDto_WhenExists() {
        ResponseEntity<ProductTypeDto> response = this.testRestTemplate.getForEntity("/api/v1/product-types/1", ProductTypeDto.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Orange T-Shirt", response.getBody().getTitle());
    }

    @Test
    public void get_ShouldReturn4xx_WhenNotFound() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.getForEntity("/api/v1/product-types/99999", ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Тип продукта не найден", response.getBody().getDetail());
    }

    @Test
    public void create_ShouldReturnOk_WhenValid() {
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "Cheese");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProductTypeDto> response = this.testRestTemplate.exchange(
                "/api/v1/product-types", HttpMethod.POST, request, ProductTypeDto.class
        );

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("Cheese", response.getBody().getTitle());
    }

    @Test
    void create_ShouldReturnValidationError_WhenTitleInvalid() {
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "s");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProblemDetail> response = this.testRestTemplate.exchange("/api/v1/product-types", HttpMethod.POST, request, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Минимальная длина названия типа продукта составляет 2 символа, а максимальная - 40", response.getBody().getDetail());
    }

    @Test
    public void update_ShouldReturnOk_WhenValid() {
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "New ProductType");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        ResponseEntity<String> response = this.testRestTemplate.exchange("/api/v1/product-types/2", HttpMethod.PATCH, request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Тип продукта обновлён", response.getBody());

        ResponseEntity<ProductTypeDto> getResponse = this.testRestTemplate.getForEntity("/api/v1/product-types/2", ProductTypeDto.class);
        Assertions.assertEquals("New ProductType", getResponse.getBody().getTitle());
    }

    @Test
    public void update_ShouldReturnValidationError_WhenTitleInvalid() {
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", ""); // пустое название
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProblemDetail> response = this.testRestTemplate.exchange("/api/v1/product-types/1", HttpMethod.PATCH, request, ProblemDetail.class);

        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void delete_ShouldReturnOk_WhenExists() {
        ResponseEntity<String> response = this.testRestTemplate.exchange("/api/v1/product-types/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Тип продукта удалён", response.getBody());
    }

    @Test
    public void delete_ShouldReturn4xx_WhenNotFound() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.exchange("/api/v1/product-types/99999", HttpMethod.DELETE, HttpEntity.EMPTY, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Тип продукта не найден", response.getBody().getDetail());
    }

    @Test
    public void assignImage_ShouldReturnOk_WhenValid() throws IOException {
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", "dummy content".getBytes());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body);

        ResponseEntity<String> response = this.testRestTemplate.postForEntity("/api/v1/product-types/1/image", request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображения загружены", response.getBody());
    }

    @Test
    public void deleteImage_ShouldReturnOk_WhenLinkValid() {
        String link = "image_link.png";
        ResponseEntity<String> response = this.testRestTemplate.exchange("/api/v1/product-types/1/image?link=" + link, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображение удалено", response.getBody());
    }
}
