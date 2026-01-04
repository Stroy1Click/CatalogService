package ru.stroy1click.product.util;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import ru.stroy1click.product.exception.*;

import java.util.List;

public class ValidationErrorUtils {

    public static String collectErrorsToString(List<FieldError> fieldErrors){
        return fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList().toString()
                .replace("[", "").replace("]", "");
    }
}
