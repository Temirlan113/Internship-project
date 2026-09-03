package com.kz.internship_project.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;


import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message){
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler({EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex){

        log.warn("Ресурс не найден: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex){

        log.error("Внутренняя ошибка сервера: ", ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){

        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        log.warn("Ошибка валидации входящих данных: {}", errorMessage);


        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler({IllegalArgumentException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception ex){

        log.warn("Некорректный аргумент: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }



    @ExceptionHandler({HttpClientErrorException.Unauthorized.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(Exception ex){
        log.warn("Ошибка аутентификации: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({HttpClientErrorException.Forbidden.class, AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<ErrorResponse> handleForbiddenException(HttpClientErrorException.Forbidden ex){
        log.warn("У вас нет прав доступа: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

}
