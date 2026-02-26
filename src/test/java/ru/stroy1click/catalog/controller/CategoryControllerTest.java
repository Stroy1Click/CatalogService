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
import ru.stroy1click.catalog.dto.CategoryDto;
import ru.stroy1click.catalog.service.category.CategoryService;
import ru.stroy1click.catalog.validator.category.CategoryCreateValidator;
import ru.stroy1click.catalog.validator.category.CategoryUpdateValidator;
import ru.stroy1click.common.validator.ImageValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(controllers = CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryCreateValidator categoryCreateValidator;

    @MockitoBean
    private CategoryUpdateValidator updateValidator;

    @MockitoBean
    private ImageValidator imageValidator;

    @Test
    public void create_WhenTitleIsEmpty_ShouldThrowValidationException() throws Exception {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "", "s");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/categories")
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
        assertEquals(
                "Минимальная длина названия категории составляет 2 символа, а максимальная - 40",
                problemDetail.getDetail()
        );
    }

    @Test
    public void update_WhenTitleIsEmpty_ShouldThrowValidationException() throws Exception {
        //Arrange
        CategoryDto dto = new CategoryDto(null, "", "s");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/categories/1")
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
        assertEquals(
                "Минимальная длина названия категории составляет 2 символа, а максимальная - 40",
                problemDetail.getDetail()
        );
    }
}
