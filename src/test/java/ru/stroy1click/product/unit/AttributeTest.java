package ru.stroy1click.product.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import ru.stroy1click.product.exception.NotFoundException;
import ru.stroy1click.product.service.attribute.impl.AttributeServiceImpl;
import java.util.Optional;
import static org.mockito.Mockito.*;

import ru.stroy1click.product.dto.AttributeDto;
import ru.stroy1click.product.entity.Attribute;
import ru.stroy1click.product.mapper.AttributeMapper;
import ru.stroy1click.product.repository.AttributeRepository;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

class AttributeTest {

    @InjectMocks
    private AttributeServiceImpl attributeService;

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private AttributeMapper mapper;

    @Mock
    private MessageSource messageSource;

    private Attribute attribute;
    private AttributeDto attributeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        this.attribute = Attribute.builder()
                .id(1)
                .title("Color")
                .build();

        this.attributeDto = new AttributeDto();
        this.attributeDto.setTitle("Color");

        when(this.messageSource.getMessage(anyString(), any(), any(Locale.class)))
                .thenReturn("Attribute not found");
    }

    @Test
    void get_ShouldReturnAttributeDto_WhenExists() {
        when(this.attributeRepository.findById(1)).thenReturn(Optional.of(this.attribute));
        when(this.mapper.toDto(this.attribute)).thenReturn(this.attributeDto);

        AttributeDto result = this.attributeService.get(1);

        assertNotNull(result);
        assertEquals("Color", result.getTitle());
        verify(this.attributeRepository).findById(1);
        verify(this.mapper).toDto(this.attribute);
    }

    @Test
    void get_ShouldThrowNotFoundException_WhenDoesNotExist() {
        when(this.attributeRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> this.attributeService.get(1));
        assertEquals("Attribute not found", exception.getMessage());
        verify(this.attributeRepository).findById(1);
    }

    @Test
    void create_ShouldCallMapperAndSave_WhenAttributeDtoProvided() {
        Attribute attributeToSave = Attribute.builder().title("Color").build();
        when(this.mapper.toEntity(this.attributeDto)).thenReturn(attributeToSave);

        this.attributeService.create(this.attributeDto);

        verify(this.mapper).toEntity(this.attributeDto);
        verify(this.attributeRepository).save(attributeToSave);
    }

    @Test
    void delete_ShouldDeleteAttribute_WhenExists() {
        when(this.attributeRepository.findById(1)).thenReturn(Optional.of(this.attribute));

        this.attributeService.delete(1);

        verify(this.attributeRepository).delete(this.attribute);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenDoesNotExist() {
        when(this.attributeRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> this.attributeService.delete(1));
        assertEquals("Attribute not found", exception.getMessage());

        verify(this.attributeRepository).findById(1);
        verify(this.attributeRepository, never()).delete(any());
    }

    @Test
    void update_ShouldUpdateAttribute_WhenExists() {
        Attribute existing = Attribute.builder().id(1).title("Old").build();
        AttributeDto dto = new AttributeDto();
        dto.setTitle("New");

        when(this.attributeRepository.findById(1)).thenReturn(Optional.of(existing));

        this.attributeService.update(1, dto);

        ArgumentCaptor<Attribute> captor = ArgumentCaptor.forClass(Attribute.class);
        verify(this.attributeRepository).save(captor.capture());
        Attribute saved = captor.getValue();
        assertEquals(1, saved.getId());
        assertEquals("New", saved.getTitle());
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenDoesNotExist() {
        AttributeDto dto = new AttributeDto();
        dto.setTitle("New");

        when(this.attributeRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> this.attributeService.update(1, dto));
        assertEquals("Attribute not found", exception.getMessage());

        verify(this.attributeRepository).findById(1);
        verify(this.attributeRepository, never()).save(any());
    }

    @Test
    void getByTitle_ShouldReturnOptionalAttribute_WhenExists() {
        when(this.attributeRepository.findByTitle("Color")).thenReturn(Optional.of(this.attribute));

        Optional<Attribute> result = this.attributeService.getByTitle("Color");

        assertTrue(result.isPresent());
        assertEquals(this.attribute, result.get());
    }

    @Test
    void getByTitle_ShouldReturnEmptyOptional_WhenDoesNotExist() {
        when(this.attributeRepository.findByTitle("NonExisting")).thenReturn(Optional.empty());

        Optional<Attribute> result = this.attributeService.getByTitle("NonExisting");

        assertFalse(result.isPresent());
    }
}

