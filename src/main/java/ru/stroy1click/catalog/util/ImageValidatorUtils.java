package ru.stroy1click.catalog.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.stroy1click.catalog.exception.ValidationException;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ImageValidatorUtils {

    private final MessageSource messageSource;

    public void validateImage(MultipartFile image){
        if(image == null){
            throw new ValidationException(
                    this.messageSource.getMessage(
                            "error.multipart-file.not_null",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }

    public void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new ValidationException(
                    this.messageSource.getMessage(
                            "error.multipart-file.not_null",
                            null,
                            Locale.getDefault()
                    )
            );
        }
    }
}
