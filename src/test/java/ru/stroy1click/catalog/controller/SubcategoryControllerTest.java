package ru.stroy1click.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.stroy1click.catalog.api.controller.SubcategoryController;
import ru.stroy1click.catalog.domain.subcategory.dto.SubcategoryDto;
import ru.stroy1click.catalog.domain.subcategory.service.SubcategoryService;
import ru.stroy1click.catalog.domain.subcategory.validator.SubcategoryCreateValidator;
import ru.stroy1click.catalog.domain.subcategory.validator.SubcategoryUpdateValidator;
import ru.stroy1click.common.validator.ImageValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(controllers = SubcategoryController.class)
public class SubcategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubcategoryService subcategoryService;

    @MockitoBean
    private SubcategoryCreateValidator createValidator;

    @MockitoBean
    private SubcategoryUpdateValidator updateValidator;

    @MockitoBean
    private ImageValidator imageValidator;


    @Test
    public void create_WhenSubcategoryDtoTitleIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image", "");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/subcategories")
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
    public void create_WhenSubcategoryDtoCategoryIdIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, null, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/subcategories")
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
    public void create_WhenSubcategoryDtoCategoryIdIsNegative_ShouldReturnValidationException() throws Exception {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image", "");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/subcategories")
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
    public void update_WhenSubcategoryDtoTitleIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, 1, "image", "");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/subcategories/1")
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
    public void update_WhenSubcategoryDtoCategoryIdIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, null, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/subcategories/1")
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
    public void update_WhenSubcategoryDtoCategoryIdIsNegative_ShouldReturnValidationException() throws Exception {
        //Arrange
        SubcategoryDto dto = new SubcategoryDto(null, -1, "image", "Title");
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/subcategories/1")
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
        assertEquals("Id категории подкатегории не может быть меньше 1", problemDetail.getDetail());
    }
}