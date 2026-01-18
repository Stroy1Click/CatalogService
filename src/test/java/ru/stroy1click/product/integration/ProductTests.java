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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.stroy1click.product.dto.ProductDto;
import ru.stroy1click.product.dto.ProductImageDto;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void getProduct_ShouldReturnProduct() {
        ResponseEntity<ProductDto> response = this.testRestTemplate.getForEntity("/api/v1/products/1", ProductDto.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("First Product", response.getBody().getTitle());
    }

    @Test
    public void createProduct_ShouldReturnOk() {
        ProductDto dto = new ProductDto(null,"111 Product","Description",200.00,true,1,1,1);
        ResponseEntity<ProductDto> response = this.testRestTemplate.
                postForEntity("/api/v1/products", dto, ProductDto.class);

        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("111 Product", response.getBody().getTitle());
    }

    @Test
    public void updateProduct_ShouldReturnOk() {
        ProductDto dto = new ProductDto(null,"Updated Product","Description",200.00,true,1,1,1);
        ResponseEntity<String> response = this.testRestTemplate.
                exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), String.class);
        Assertions.assertEquals("Продукт обновлён", response.getBody());

        ResponseEntity<ProductDto> getResponse = this.testRestTemplate.getForEntity("/api/v1/products/2", ProductDto.class);
        Assertions.assertEquals("Updated Product", getResponse.getBody().getTitle());
    }

    @Test
    public void deleteProduct_ShouldReturnOk() {
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/products/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        Assertions.assertEquals("Продукт удалён", response.getBody());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void getProduct_NotFound_ShouldReturnProblemDetail() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .getForEntity("/api/v1/products/10000000", ProblemDetail.class);

        Assertions.assertEquals("Не найдено", response.getBody().getTitle());
        Assertions.assertEquals("Продукт не найден", response.getBody().getDetail());
    }

    @Test
    public void createProduct_InvalidTitle_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"","Description",200.00,true,1,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .postForEntity("/api/v1/products", dto, ProblemDetail.class);
        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void createProduct_NegativePrice_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",-200.00,true,1,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .postForEntity("/api/v1/products", dto, ProblemDetail.class);
        Assertions.assertEquals("Цена продукта не может быть отрицательной", response.getBody().getDetail());
    }

    @Test
    public void createProduct_NullInStock_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,null,1,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .postForEntity("/api/v1/products", dto, ProblemDetail.class);
        Assertions.assertEquals("Статус наличия продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void createProduct_NullCategoryId_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,true,null,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .postForEntity("/api/v1/products", dto, ProblemDetail.class);
        Assertions.assertEquals("Id категории продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void createProduct_NullSubcategoryId_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,true,1,null,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .postForEntity("/api/v1/products", dto, ProblemDetail.class);
        Assertions.assertEquals("Id подкатегории продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void createProduct_NullProductTypeId_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,true,1,1,null);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .postForEntity("/api/v1/products", dto, ProblemDetail.class);
        Assertions.assertEquals("Id типа продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void updateProduct_InvalidTitle_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"","Description",200.00,true,1,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);
        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void updateProduct_NegativePrice_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",-200.00,true,1,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);
        Assertions.assertEquals("Цена продукта не может быть отрицательной", response.getBody().getDetail());
    }

    @Test
    public void updateProduct_NullInStock_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,null,1,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);
        Assertions.assertEquals("Статус наличия продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void updateProduct_NullCategoryId_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,true,null,1,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);
        Assertions.assertEquals("Id категории продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void updateProduct_NullSubcategoryId_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,true,1,null,1);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);
        Assertions.assertEquals("Id подкатегории продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void updateProduct_NullProductTypeId_ShouldReturnValidationException() {
        ProductDto dto = new ProductDto(null,"Third Product","Description",200.00,true,1,1,null);
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);
        Assertions.assertEquals("Id типа продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void assignImages_ShouldReturnOk() {
        MockMultipartFile image = new MockMultipartFile("images","image.png","image/png", new byte[]{1,2,3});
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("images", image.getResource());

        ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/api/v1/products/1/images", body, String.class);
        Assertions.assertEquals("Изображения загружены", response.getBody());
    }

    @Test
    public void deleteImage_ShouldReturnOk() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.exchange(
                "/api/v1/products/1/images?link=image.png",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                ProblemDetail.class
        );
        Assertions.assertEquals("Изображение продукта не найдено", response.getBody().getDetail());
    }

    @Test
    public void getImages_ShouldReturnList() {
        ResponseEntity<ProductImageDto[]> response = this.testRestTemplate
                .getForEntity("/api/v1/products/1/images", ProductImageDto[].class);
        Assertions.assertNotNull(response.getBody());
    }
}