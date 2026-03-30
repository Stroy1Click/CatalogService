package ru.stroy1click.catalog.controller;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.stroy1click.catalog.config.TestcontainersConfiguration;
import ru.stroy1click.catalog.domain.product.dto.ProductDto;
import ru.stroy1click.catalog.domain.product.image.dto.ProductImageDto;
import ru.stroy1click.catalog.domain.common.service.StorageService;
import ru.stroy1click.common.dto.Unit;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private StorageService storageService;

    @Test
    public void get_WhenProductExists_ShouldReturnProduct() {
        //Act
        ResponseEntity<ProductDto> response =
                this.testRestTemplate.getForEntity("/api/v1/products/1", ProductDto.class);

        //Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("First Product", response.getBody().getTitle());
    }

    @Test
    public void get_WhenProductDoesNotExist_ShouldThrowNotFoundException() {
        //Act
        ResponseEntity<ProblemDetail> response =
                this.testRestTemplate.getForEntity("/api/v1/products/99999", ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Не найдено", response.getBody().getTitle());
    }

    @Test
    public void create_WhenValidDataProvided_ShouldReturnOk() {
        //Arrange
        ProductDto dto = new ProductDto(null,"111 Product","Description", BigDecimal.valueOf(200.00), Unit.KG,true,1,1,1);

        //Act
        ResponseEntity<ProductDto> response = this.testRestTemplate.
                postForEntity("/api/v1/products", dto, ProductDto.class);

        //Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals("111 Product", response.getBody().getTitle());
    }

    @Test
    public void create_WhenProductAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"First Product","Description",BigDecimal.valueOf(200.00), Unit.KG,true,1,1,1);
        HttpEntity<ProductDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products", HttpMethod.POST, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void update_WhenProductExistsAndValidDataProvided_ShouldReturnOk() {
        //Arrange
        ProductDto dto = new ProductDto(null,"Updated Product 1","Description",BigDecimal.valueOf(200.00), Unit.KG,true,1,1,1);

        //Act
        ResponseEntity<String> response = this.testRestTemplate.
                exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), String.class);

        //Assert
        assertEquals("Продукт обновлён", response.getBody());
        ResponseEntity<ProductDto> getResponse = this.testRestTemplate.getForEntity("/api/v1/products/2", ProductDto.class);
        assertEquals("Updated Product 1", getResponse.getBody().getTitle());
    }

    @Test
    public void update_WhenProductAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"First Product","Description",BigDecimal.valueOf(200.00), Unit.KG,true,1,1,1);
        HttpEntity<ProductDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void delete_WhenProductExists_ShouldReturnOk() {
        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/products/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        //Assert
        assertEquals("Продукт удалён", response.getBody());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void update_WhenProductDtoTitleIsEmpty_ShouldThrowValidationException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"","Description",BigDecimal.valueOf(200.00), Unit.KG,true,1,1,1);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);

        //Assert
        assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void update_WhenProductDtoPriceIsNegative_ShouldThrowValidationException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"Third Product","Description",BigDecimal.valueOf(-200.00), Unit.KG,true,1,1,1);

        //Arrange
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);

        //Assert
        assertEquals("Цена продукта не может быть меньше 1", response.getBody().getDetail());
    }

    @Test
    public void update_WhenProductDtoInStockIsNull_ShouldThrowValidationException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"Third Product","Description",BigDecimal.valueOf(200.00), Unit.KG,null,1,1,1);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);

        //Assert
        assertEquals("Статус наличия продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void update_WhenProductDtoCategoryIdIsNull_ShouldThrowValidationException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"Third Product","Description",BigDecimal.valueOf(200.00), Unit.KG,true,null,1,1);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);

        //Assert
        assertEquals("Id категории продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void update_WhenProductDtoSubcategoryIdIsNull_ShouldThrowValidationException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"Third Product","Description",BigDecimal.valueOf(200.00), Unit.KG,true,1,null,1);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);

        //Assert
        assertEquals("Id подкатегории продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void update_WhenProductDtoProductTypeIdIsNull_ShouldThrowValidationException() {
        //Arrange
        ProductDto dto = new ProductDto(null,"Third Product","Description",BigDecimal.valueOf(200.00), Unit.KG,true,1,1,null);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/products/2", HttpMethod.PATCH, new HttpEntity<>(dto), ProblemDetail.class);

        //Assert
        assertEquals("Id типа продукта не может быть пустым", response.getBody().getDetail());
    }

    @Test
    public void assignImages_WhenProductExists_ShouldReturnOk() {
        //Arrange
        MockMultipartFile image = new MockMultipartFile("images","image.png","image/png", new byte[]{1,2,3});
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("images", image.getResource());
        when(this.storageService.uploadImages(anyList())).thenReturn(List.of("link.png"));

        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/api/v1/products/1/images", body, String.class);

        //Assert
        assertEquals("Изображения загружены", response.getBody());
    }

    @Test
    public void deleteImage_WhenProductExistsAndImageDoesNotExist_ShouldReturnOk() {
        //Arrange
        String notExistsLink = "image.png";
        doNothing().when(this.storageService).deleteImage(anyString());

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate.exchange(
                "/api/v1/products/1/images?link=" + notExistsLink,
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                ProblemDetail.class
        );

        //Assert
        assertEquals("Изображение продукта %s не найдено".formatted(notExistsLink), response.getBody().getDetail());
    }

    @Test
    public void getImages_WhenProductExists_ShouldReturnList() {
        //Act
        ResponseEntity<ProductImageDto[]> response = this.testRestTemplate
                .getForEntity("/api/v1/products/1/images", ProductImageDto[].class);

        //Assert
        Assertions.assertNotNull(response.getBody());
    }
}