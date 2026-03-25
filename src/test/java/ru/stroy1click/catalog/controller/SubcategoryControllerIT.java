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
import ru.stroy1click.catalog.dto.SubcategoryDto;
import ru.stroy1click.catalog.service.storage.StorageService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubcategoryControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private StorageService storageService;

    @Test
    public void get_WhenSubcategoryExists_ShouldReturnSubcategoryDto() {
        //Act
        ResponseEntity<SubcategoryDto> response = this.testRestTemplate
                .getForEntity("/api/v1/subcategories/1", SubcategoryDto.class);

        //Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("T-shirts", response.getBody().getTitle());
    }

    @Test
    public void get_WhenSubcategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        int notExistId = 99999;

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .getForEntity("/api/v1/subcategories/" + notExistId, ProblemDetail.class);

        //Assert
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Подкатегория с id %d не найдена".formatted(notExistId), response.getBody().getDetail());
    }

    @Test
    public void create_WhenProvidedDataIsValid_ShouldReturnCreatedSubcategory() {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "Cement");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<SubcategoryDto> response = this.testRestTemplate
                .exchange("/api/v1/subcategories", HttpMethod.POST, request, SubcategoryDto.class);

        //Assert
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("Cement", response.getBody().getTitle());
    }

    @Test
    public void create_WhenSubcategoryAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "T-shirts");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/subcategories", HttpMethod.POST, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void update_WhenValidDataProvidedAndSubcategoryExists_ShouldReturnOk() {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "New Subcategory");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/2", HttpMethod.PATCH, request, String.class);

        //Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Подкатегория обновлена", response.getBody());

        ResponseEntity<SubcategoryDto> getResponse = this.testRestTemplate
                .getForEntity("/api/v1/subcategories/2", SubcategoryDto.class);
        Assertions.assertEquals("New Subcategory", getResponse.getBody().getTitle());
    }

    @Test
    public void update_WhenSubcategoryAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "T-shirts");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/2", HttpMethod.PATCH, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void delete_WhenSubcategoryExists_ShouldReturnOk() {
        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        //Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Подкатегория удалена", response.getBody());
    }

    @Test
    public void delete_WhenSubcategoryDoesNotExist_ShouldReturn4xx() {
        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/99999", HttpMethod.DELETE, HttpEntity.EMPTY, ProblemDetail.class);

        //Assert
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    public void assignImage_WhenImageValidAndSubcategoryExists_ShouldReturnOk() throws IOException {
        //Arrange
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.png", "image/png", "dummy content".getBytes()
        );
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body);
        when(this.storageService.uploadImage(any(MultipartFile.class))).thenReturn("link.png");

        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/api/v1/subcategories/1/image", request, String.class);

        //Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображения загружены", response.getBody());
    }

    @Test
    public void deleteImage_WhenLinkValidAndSubcategoryExists_ShouldReturnOk() {
        //Act
        String link = "subcategory_image.png";
        doNothing().when(this.storageService).deleteImage(anyString());

        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/1/image?link=" + link,
                        HttpMethod.DELETE,
                        HttpEntity.EMPTY,
                        String.class);

        //Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображение удалено", response.getBody());
    }
}
