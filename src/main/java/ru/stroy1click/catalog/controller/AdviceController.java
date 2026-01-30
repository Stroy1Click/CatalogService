package ru.stroy1click.catalog.controller;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.stroy1click.catalog.exception.*;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class AdviceController {

    private final MessageSource messageSource;

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail problemDetail(NotFoundException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.not_found",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail problemDetail(ValidationException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.validation",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ProblemDetail problemDetail(AlreadyExistsException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.already_exist",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ProblemDetail handleException(RequestNotPermitted exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, exception.getMessage()
        );
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.too_many_requests",
                        null,
                        Locale.getDefault()
                )
        );
        problemDetail.setDetail(
                this.messageSource.getMessage(
                        "error.details.too_many_requests",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ProblemDetail handleException(ServiceUnavailableException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                this.messageSource.getMessage(
                        "error.details.service_unavailable",
                        null,
                        Locale.getDefault()
                )
        );
        problemDetail.setTitle(this.messageSource.getMessage(
                "error.title.service_unavailable",
                null,
                Locale.getDefault()
        ));
        return problemDetail;
    }

    @ExceptionHandler(ServiceErrorResponseException.class)
    public ProblemDetail handleException(ServiceErrorResponseException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage()
        );
        problemDetail.setTitle(this.messageSource.getMessage(
                "error.title.service_unavailable",
                null,
                Locale.getDefault()
        ));
        return problemDetail;
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ProblemDetail handleException(CallNotPermittedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                this.messageSource.getMessage(
                        "error.details.service_unavailable",
                        null,
                        Locale.getDefault()
                )
        );
        problemDetail.setTitle(this.messageSource.getMessage(
                "error.title.service_unavailable",
                null,
                Locale.getDefault()
        ));
        return problemDetail;
    }
}
