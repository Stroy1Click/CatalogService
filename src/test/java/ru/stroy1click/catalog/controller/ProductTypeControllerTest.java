package ru.stroy1click.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.stroy1click.catalog.api.controller.ProductTypeController;
import ru.stroy1click.catalog.domain.producttype.dto.ProductTypeDto;
import ru.stroy1click.catalog.domain.producttype.service.ProductTypeService;
import ru.stroy1click.catalog.domain.producttype.validator.ProductTypeCreateValidator;
import ru.stroy1click.catalog.domain.producttype.validator.ProductTypeUpdateValidator;
import ru.stroy1click.common.validator.ImageValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(controllers = ProductTypeController.class)
public class ProductTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductTypeService productTypeService;

    @MockitoBean
    private ProductTypeCreateValidator createValidator;

    @MockitoBean
    private ProductTypeUpdateValidator updateValidator;

    @MockitoBean
    private ImageValidator imageValidator;

    @Test
    void create_WhenProductTypeDtoTitleIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, 1, "image", "");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();

        //Assert
        assertEquals(400, status);
        assertEquals("Ошибка валидации", problemDetail.getTitle());
    }

    @Test
    void create_WhenProductTypeDtoSubcategoryIdIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, null, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();

        //Assert
        assertEquals(400, status);
        assertEquals("Ошибка валидации", problemDetail.getTitle());
    }

    @Test
    void create_WhenProductTypeDtoSubcategoryIdIsNegative_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, -1, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/product-types")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();

        //Assert
        assertEquals(400, status);
        assertEquals("Id подкатегории типа продукта не может меньше 1",
                problemDetail.getDetail());
    }

    @Test
    public void update_WhenProductTypeDtoTitleIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, 1, "image", "");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/product-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();

        //Assert
        assertEquals(400, status);
        assertEquals("Ошибка валидации", problemDetail.getTitle());
    }

    @Test
    void update_WhenProductTypeDtoSubcategoryIdIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, null, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/product-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();

        //Assert
        assertEquals(400, status);
        assertEquals("Id подкатегории типа продукта не может быть пустым",
                problemDetail.getDetail());
    }

    @Test
    void update_WhenProductTypeDtoSubcategoryIdIsNegative_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductTypeDto dto = new ProductTypeDto(null, -1, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/product-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();

        //Assert
        assertEquals(400, status);
        assertEquals("Id подкатегории типа продукта не может меньше 1",
                problemDetail.getDetail());
    }
}