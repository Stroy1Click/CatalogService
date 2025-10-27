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
import ru.stroy1click.product.dto.AttributeDto;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AttributeTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void get_ShouldReturnAttribute_WhenAttributeExists() {
        ResponseEntity<AttributeDto> responseEntity = this.testRestTemplate.getForEntity(
                "/api/v1/attributes/1", AttributeDto.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Size", responseEntity.getBody().getTitle());
    }

    @Test
    public void create_ShouldCreateAttribute_WhenDtoIsValid() {
        AttributeDto dto = new AttributeDto(null, "Material");
        HttpEntity<AttributeDto> request = new HttpEntity<>(dto);

        ResponseEntity<String> responseEntity = this.testRestTemplate.postForEntity(
                "/api/v1/attributes", request, String.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Атрибут создан", responseEntity.getBody());
    }

    @Test
    public void update_ShouldUpdateAttribute_WhenDtoIsValid() {
        AttributeDto dto = new AttributeDto(null, "Size");
        HttpEntity<AttributeDto> request = new HttpEntity<>(dto);

        ResponseEntity<String> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/attributes/2", HttpMethod.PATCH, request, String.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Атрибут обновлён", responseEntity.getBody());

        ResponseEntity<AttributeDto> getResponse = this.testRestTemplate.getForEntity(
                "/api/v1/attributes/2", AttributeDto.class
        );
        Assertions.assertEquals("Size", getResponse.getBody().getTitle());
    }

    @Test
    public void delete_ShouldDeleteAttribute_WhenAttributeExists() {
        ResponseEntity<String> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/attributes/3", HttpMethod.DELETE, null, String.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Атрибут удалён", responseEntity.getBody());
    }

    @Test
    public void get_ShouldReturnNotFound_WhenAttributeDoesNotExist() {
        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.getForEntity(
                "/api/v1/attributes/1000000", ProblemDetail.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Не найдено", responseEntity.getBody().getTitle());
    }

    @Test
    public void create_ShouldReturnValidationError_WhenTitleIsEmpty() {
        AttributeDto dtoWithEmptyTitle = new AttributeDto(null, "");
        HttpEntity<AttributeDto> requestEmpty = new HttpEntity<>(dtoWithEmptyTitle);

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/attributes", HttpMethod.POST, requestEmpty, ProblemDetail.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
    }

    @Test
    public void update_ShouldReturnValidationError_WhenTitleIsEmpty() {
        AttributeDto dtoWithEmptyTitle = new AttributeDto(null, "");
        HttpEntity<AttributeDto> requestEmpty = new HttpEntity<>(dtoWithEmptyTitle);

        ResponseEntity<ProblemDetail> responseEntity = this.testRestTemplate.exchange(
                "/api/v1/attributes/1", HttpMethod.PATCH, requestEmpty, ProblemDetail.class
        );

        Assertions.assertTrue(responseEntity.getStatusCode().is4xxClientError());
        Assertions.assertEquals("Ошибка валидации", responseEntity.getBody().getTitle());
    }
}

