package ru.stroy1click.catalog.controller;

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
import ru.stroy1click.catalog.domain.category.dto.CategoryDto;
import ru.stroy1click.catalog.domain.common.service.StorageService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerIT {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private StorageService storageService;

    @Test
    public void get_WhenCategoryExists_ShouldReturnCategoryDto() {
        //Act
        ResponseEntity<CategoryDto> response = this.testRestTemplate
                .getForEntity("/api/v1/categories/1", CategoryDto.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Water", response.getBody().getTitle());
    }

    @Test
    public void get_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Arrange
        int notExistsId = 99999;

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .getForEntity("/api/v1/categories/" + notExistsId, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Категория с id %d не найдена".formatted(notExistsId), response.getBody().getDetail());
    }


    @Test
    public void create_WhenValidDataProvided_ShouldReturnCreatedCategoryDto() {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "Image link", "Cement");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<CategoryDto> response = this.testRestTemplate
                .exchange("/api/v1/categories", HttpMethod.POST, request, CategoryDto.class);

        //Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Cement", response.getBody().getTitle());
        assertNotNull(response.getBody().getId());
    }

    @Test
    public void create_WhenCategoryAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "Image link", "Water");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories", HttpMethod.POST, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void update_WhenCategoryExistsAndValidDataProvided_ShouldReturnOk() {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "Image link", "New Category");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/categories/2", HttpMethod.PATCH, request, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Категория обновлена", response.getBody());

        ResponseEntity<CategoryDto> getResponse = this.testRestTemplate
                .getForEntity("/api/v1/categories/2", CategoryDto.class);
        assertEquals("New Category", getResponse.getBody().getTitle());
    }

    @Test
    public void update_WhenCategoryAlreadyExists_ShouldThrowAlreadyExistsException() {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "Image link", "Water");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories/2", HttpMethod.PATCH, request, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals("Объект уже существует", response.getBody().getTitle());
    }

    @Test
    public void update_WhenTitleIsEmpty_ShouldThrowValidationException() {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "Image link", "");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories/1", HttpMethod.PATCH, request, ProblemDetail.class);

        //Assert
        assertEquals("Ошибка валидации", response.getBody().getTitle());
    }


    @Test
    public void delete_WhenCategoryExists_ShouldReturnOk() {
        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/categories/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Категория удалена", response.getBody());
    }

    @Test
    public void delete_WhenCategoryDoesNotExist_ShouldThrowNotFoundException() {
        //Act
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories/99999", HttpMethod.DELETE, HttpEntity.EMPTY, ProblemDetail.class);

        //Assert
        assertTrue(response.getStatusCode().is4xxClientError());
        assertNotNull(response.getBody());
    }


    @Test
    public void assignImage_WhenImageValidAndCategoryExists_ShouldReturnOk() throws IOException {
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
                .postForEntity("/api/v1/categories/1/image", request, String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Изображения загружены",
                response.getBody());
    }

    @Test
    public void deleteImage_WhenLinkValidAndCategoryExists_ShouldReturnOk() {
        //Arrange
        String link = "image_link.png";
        doNothing().when(this.storageService).deleteImage(anyString());

        //Act
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/categories/1/image?link=" + link,
                        HttpMethod.DELETE,
                        HttpEntity.EMPTY,
                        String.class);

        //Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Изображение удалено", response.getBody());
    }
}

