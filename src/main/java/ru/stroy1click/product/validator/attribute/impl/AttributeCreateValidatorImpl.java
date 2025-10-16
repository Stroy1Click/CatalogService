package ru.stroy1click.product.validator.attribute.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import ru.stroy1click.product.dto.AttributeDto;
import ru.stroy1click.product.exception.AlreadyExistsException;
import ru.stroy1click.product.service.attribute.AttributeService;
import ru.stroy1click.product.validator.attribute.AttributeCreateValidator;
import ru.stroy1click.product.validator.base.CreateValidator;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeCreateValidatorImpl implements AttributeCreateValidator {

    private final AttributeService attributeService;

    private final MessageSource messageSource;

    @Override
    public void validate(AttributeDto attributeDto) {
        log.info("validate {}", attributeDto);
        if(this.attributeService.getByTitle(attributeDto.getTitle()).isPresent()){
            throw new AlreadyExistsException(
                    this.messageSource.getMessage(
                            "error.attribute.create.validate",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
