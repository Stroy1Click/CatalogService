package ru.stroy1click.product.integration;

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
import ru.stroy1click.product.dto.CategoryDto;

import java.io.IOException;

@Import({TestcontainersConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void get_ShouldReturnCategoryDto_WhenCategoryExists() {
        ResponseEntity<CategoryDto> response = this.testRestTemplate
                .getForEntity("/api/v1/categories/1", CategoryDto.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("Water", response.getBody().getTitle());
    }

    @Test
    public void get_ShouldReturn4xx_WhenCategoryDoesNotExist() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .getForEntity("/api/v1/categories/99999", ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Категория не найдена", response.getBody().getDetail());
    }

    // ---------- CREATE ----------

    @Test
    public void create_ShouldReturnOk_WhenCategoryDtoIsValid() {
        CategoryDto dto = new CategoryDto(null, "Image link", "Cement");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<CategoryDto> response = this.testRestTemplate
                .exchange("/api/v1/categories", HttpMethod.POST, request, CategoryDto.class);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("Cement", response.getBody().getTitle());
        Assertions.assertNotNull(response.getBody().getId());
    }

    @Test
    public void create_ShouldReturnValidationError_WhenTitleInvalid() {
        CategoryDto dto = new CategoryDto(null, "Image link", "s"); // too short
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories", HttpMethod.POST, request, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(
                "Минимальная длина названия категории составляет 2 символа, а максимальная - 40",
                response.getBody().getDetail()
        );
    }

    // ---------- UPDATE ----------

    @Test
    public void update_ShouldReturnOk_WhenCategoryDtoIsValid() {
        CategoryDto dto = new CategoryDto(null, "Image link", "New Category");
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/categories/2", HttpMethod.PATCH, request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Категория обновлена", response.getBody());

        ResponseEntity<CategoryDto> getResponse = this.testRestTemplate
                .getForEntity("/api/v1/categories/2", CategoryDto.class);
        Assertions.assertEquals("New Category", getResponse.getBody().getTitle());
    }

    @Test
    public void update_ShouldReturnValidationError_WhenTitleInvalid() {
        CategoryDto dto = new CategoryDto(null, "Image link", ""); // empty
        HttpEntity<CategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories/1", HttpMethod.PATCH, request, ProblemDetail.class);

        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    // ---------- DELETE ----------

    @Test
    public void delete_ShouldReturnOk_WhenCategoryExists() {
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/categories/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Категория удалена", response.getBody());
    }

    @Test
    public void delete_ShouldReturn4xx_WhenCategoryDoesNotExist() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/categories/99999", HttpMethod.DELETE, HttpEntity.EMPTY, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertNotNull(response.getBody());
    }

    // ---------- IMAGE ----------

    @Test
    public void assignImage_ShouldReturnOk_WhenImageValid() throws IOException {
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

        ResponseEntity<String> response = this.testRestTemplate
                .postForEntity("/api/v1/categories/1/image", request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображения загружены",
                response.getBody());
    }

    @Test
    public void deleteImage_ShouldReturnOk_WhenLinkValid() {
        String link = "image_link.png";
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/categories/1/image?link=" + link,
                        HttpMethod.DELETE,
                        HttpEntity.EMPTY,
                        String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображение удалено", response.getBody());
    }
}

