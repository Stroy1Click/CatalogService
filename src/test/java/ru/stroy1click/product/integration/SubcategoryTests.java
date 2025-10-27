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
import ru.stroy1click.product.dto.SubcategoryDto;

import java.io.IOException;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubcategoryTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void get_ShouldReturnSubcategoryDto_WhenExists() {
        ResponseEntity<SubcategoryDto> response = this.testRestTemplate
                .getForEntity("/api/v1/subcategories/1", SubcategoryDto.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals("T-shirts", response.getBody().getTitle());
    }

    @Test
    public void get_ShouldReturn4xx_WhenNotExists() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .getForEntity("/api/v1/subcategories/99999", ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Подкатегория не найдена", response.getBody().getDetail());
    }

    @Test
    public void create_ShouldReturnCreated_WhenDtoIsValid() {
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "Cement");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories", HttpMethod.POST, request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Подкатегория создана", response.getBody());
    }

    @Test
    public void create_ShouldReturnValidationError_WhenTitleInvalid() {
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "s");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/subcategories", HttpMethod.POST, request, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(
                "Минимальная длина названия подкатегории составляет 2 символа, а максимальная - 40",
                response.getBody().getDetail()
        );
    }

    @Test
    public void update_ShouldReturnOk_WhenDtoIsValid() {
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "New Subcategory");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/2", HttpMethod.PATCH, request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Подкатегория обновлена", response.getBody());

        ResponseEntity<SubcategoryDto> getResponse = this.testRestTemplate
                .getForEntity("/api/v1/subcategories/2", SubcategoryDto.class);
        Assertions.assertEquals("New Subcategory", getResponse.getBody().getTitle());
    }

    @Test
    public void update_ShouldReturnValidationError_WhenTitleInvalid() {
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image.png", "");
        HttpEntity<SubcategoryDto> request = new HttpEntity<>(dto);

        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/1", HttpMethod.PATCH, request, ProblemDetail.class);

        Assertions.assertEquals("Ошибка валидации", response.getBody().getTitle());
    }

    @Test
    public void delete_ShouldReturnOk_WhenExists() {
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/3", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Подкатегория удалена", response.getBody());
    }

    @Test
    public void delete_ShouldReturn4xx_WhenNotExists() {
        ResponseEntity<ProblemDetail> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/99999", HttpMethod.DELETE, HttpEntity.EMPTY, ProblemDetail.class);

        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertNotNull(response.getBody());
    }

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
                .postForEntity("/api/v1/subcategories/1/image", request, String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображения загружены", response.getBody());
    }

    @Test
    public void deleteImage_ShouldReturnOk_WhenLinkValid() {
        String link = "subcategory_image.png";
        ResponseEntity<String> response = this.testRestTemplate
                .exchange("/api/v1/subcategories/1/image?link=" + link,
                        HttpMethod.DELETE,
                        HttpEntity.EMPTY,
                        String.class);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Изображение удалено", response.getBody());
    }
}
