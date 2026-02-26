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
import ru.stroy1click.catalog.dto.ProductDto;
import ru.stroy1click.catalog.service.product.ProductImageService;
import ru.stroy1click.catalog.service.product.ProductPaginationService;
import ru.stroy1click.catalog.service.product.ProductService;
import ru.stroy1click.catalog.validator.product.ProductCreateValidator;
import ru.stroy1click.catalog.validator.product.ProductUpdateValidator;
import ru.stroy1click.common.dto.Unit;
import ru.stroy1click.common.validator.ImageValidator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(controllers = ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductCreateValidator createValidator;

    @MockitoBean
    private ProductUpdateValidator updateValidator;

    @MockitoBean
    private ProductPaginationService productPaginationService;

    @MockitoBean
    private ProductImageService productImageService;

    @MockitoBean
    private ImageValidator imageValidator;

    @Test
    public void create_WhenProductDtoTitleIsEmpty_ShouldReturnValidationException() throws Exception {
        //Arrange
        ProductDto dto = ProductDto.builder()
                .title("")
                .description("description")
                .inStock(true)
                .price(BigDecimal.ONE)
                .unit(Unit.KG)
                .categoryId(1)
                .subcategoryId(1)
                .productTypeId(1)
                .build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/products")
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
    public void create_WhenProductDtoPriceIsNegative_ShouldThrowValidationException() throws Exception {
        //Arrange
        ProductDto dto = ProductDto.builder()
                .title("Product")
                .description("description")
                .inStock(true)
                .price(BigDecimal.valueOf(-1))
                .unit(Unit.KG)
                .categoryId(1)
                .subcategoryId(1)
                .productTypeId(1)
                .build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/products")
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
        assertEquals("Цена продукта не может быть меньше 1", problemDetail.getDetail());
    }

    @Test
    public void create_WhenProductDtoInStockIsNull_ShouldThrowValidationException() throws Exception {
        //Arrange
        ProductDto dto = ProductDto.builder()
                .title("Title")
                .description("description")
                .price(BigDecimal.ONE)
                .unit(Unit.KG)
                .categoryId(1)
                .subcategoryId(1)
                .productTypeId(1)
                .build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/products")
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
        assertEquals("Статус наличия продукта не может быть пустым", problemDetail.getDetail());
    }

    @Test
    public void create_WhenProductDtoCategoryIdIsNull_ShouldThrowValidationException() throws Exception {
        //Arrange
        ProductDto dto = ProductDto.builder()
                .title("Title")
                .description("description")
                .inStock(true)
                .price(BigDecimal.ONE)
                .unit(Unit.KG)
                .subcategoryId(1)
                .productTypeId(1)
                .build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto));

        //Act
        MvcResult result = this.mockMvc.perform(requestBuilder).andReturn();
        String string = result.getResponse().getContentAsString();
        ProblemDetail problemDetail = new ObjectMapper().readValue(string, ProblemDetail.class);
        int status = result.getResponse().getStatus();;

        //Assert
        assertEquals(400, status);
        assertEquals("Id категории продукта не может быть пустым", problemDetail.getDetail());
    }

    @Test
    public void create_WhenProductDtoSubcategoryIdIsNull_ShouldThrowValidationException() throws Exception {
        //Arrange
        ProductDto dto = ProductDto.builder()
                .title("Title")
                .description("description")
                .inStock(true)
                .price(BigDecimal.ONE)
                .unit(Unit.KG)
                .categoryId(1)
                .productTypeId(1)
                .build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/products")
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
        assertEquals("Id подкатегории продукта не может быть пустым", problemDetail.getDetail());
    }

    @Test
    public void create_WhenProductDtoProductTypeIdIsNull_ShouldThrowValidationException() throws Exception {
        //Arrange
        ProductDto dto = ProductDto.builder()
                .title("Title")
                .description("description")
                .inStock(true)
                .price(BigDecimal.ONE)
                .unit(Unit.KG)
                .categoryId(1)
                .subcategoryId(1)
                .build();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/v1/products")
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
        assertEquals("Id типа продукта не может быть пустым", problemDetail.getDetail());
    }
}