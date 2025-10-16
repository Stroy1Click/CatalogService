package ru.stroy1click.product.validator.attribute.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.AttributeDto;
import ru.stroy1click.product.entity.Attribute;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.attribute.AttributeService;
import ru.stroy1click.product.validator.attribute.AttributeUpdateValidator;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeUpdateValidatorImpl implements AttributeUpdateValidator {

    private final AttributeService attributeService;

    private final MessageSource messageSource;

    @Override
    public void validate(AttributeDto dto) {
        log.info("validate {}", dto);
        Optional<Attribute> foundAttribute= this.attributeService.getByTitle(dto.getTitle());

        if(foundAttribute.isPresent() && !Objects.equals(dto.getId(), foundAttribute.get().getId())
                && dto.getTitle().equalsIgnoreCase(foundAttribute.get().getTitle())){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.attribute.update.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
