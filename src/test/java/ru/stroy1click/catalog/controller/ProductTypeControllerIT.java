package ru.stroy1click.catalog.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.config.TestcontainersConfiguration;
import ru.stroy1click.catalog.dto.ProductTypeDto;
import ru.stroy1click.catalog.service.storage.StorageService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductTypeControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private StorageService storageService;

    @Test
    public void get_WhenProductTypeExists_ShouldReturnProductTypeDto() {
        //Act
        ResponseEntity<ProductTypeDto> response =
                this.testRestTemplate.getForEntity("/api/v1/product-types/1", ProductTypeDto.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Orange T-Shirt", response.getBody().getTitle());
    }

    @Test
    public void get_WhenProductTypeDoesNotExist_ShouldThrowNotFoundExceptuon() {
        //Act
        ResponseEntity<ProblemDetail> response =
                this.testRestTemplate.getForEntity("/api/v1/product-types/99999", ProblemDetail.class);

        //Assert
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Тип продукта не найден", response.getBody().getDetail());
    }

    @Test
    public void create_WhenValidDataProvided_ShouldReturnOk() {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "Cheese");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProductTypeDto> response = this.testRestTemplate.exchange(
                "/api/v1/product-types", HttpMethod.POST, request, ProductTypeDto.class
        );

        //Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Cheese", response.getBody().getTitle());
    }

    @Test
    public void create_WhenProductTypeAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "Orange T-Shirt");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/product-types", HttpMethod.POST, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void update_WhenValidDataProvidedAndProductTypeExists_WhenValid() {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "New ProductType");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<String> response = this.testRestTemplate.exchange("/api/v1/product-types/2", HttpMethod.PATCH, request, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Тип продукта обновлён", response.getBody());

        ResponseEntity<ProductTypeDto> getResponse = this.testRestTemplate.getForEntity("/api/v1/product-types/2", ProductTypeDto.class);
        assertEquals("New ProductType", getResponse.getBody().getTitle());
    }

    @Test
    public void update_WhenProductTypeAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, 1, "Image link", "Orange T-Shirt");
        HttpEntity<ProductTypeDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/product-types/2", HttpMethod.PATCH, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void delete_WhenProductTypeExists_ShouldReturnOk() {
        //Act
        ResponseEntity<String> response =
                this.testRestTemplate.exchange("/api/v1/product-types/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Тип продукта удалён", response.getBody());
    }

    @Test
    public void delete_WhenProductTypeDoesNotExist_ShouldThrowNotFoundException() {
        //Act
        ResponseEntity<ProblemDetail> response =
                this.testRestTemplate.exchange("/api/v1/product-types/99999", HttpMethod.DELETE, HttpEntity.EMPTY, ProblemDetail.class);

        //Assert
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Тип продукта не найден", response.getBody().getDetail());
    }

    @Test
    public void assignImage_WhenImageValidAndProductTypeExists_ShouldReturnOk() throws IOException {
        //Arrange
        MockMultipartFile file = new MockMultipartFile("image", "test.png", "image/png", "dummy content".getBytes());
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        when(this.storageService.uploadImage(any(MultipartFile.class))).thenReturn("link.png");
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body);

        //Act
        ResponseEntity<String> response = this.testRestTemplate.postForEntity("/api/v1/product-types/1/image", request, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Изображения загружены", response.getBody());
    }

    @Test
    public void deleteImage_WhenLinkValidAndProductTypeExists_ShouldReturnOk() {
        //Arrange
        String link = "image_link.png";
        doNothing().when(this.storageService).deleteImage(anyString());

        //Act
        ResponseEntity<String> response = this.testRestTemplate.exchange("/api/v1/product-types/1/image?link=" + link, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Изображение удалено", response.getBody());
    }
}
