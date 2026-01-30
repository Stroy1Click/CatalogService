package ru.stroy1click.catalog.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;

import java.util.List;

public class ValidationErrorUtils {

    public static String collectErrorsToString(List<FieldError> fieldErrors){
        return fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList().toString()
                .replace("[", "").replace("]", "");
    }
}
