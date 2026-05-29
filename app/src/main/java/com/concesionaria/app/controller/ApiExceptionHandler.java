package com.concesionaria.app.controller;

import com.concesionaria.app.service.BitacoraNoSqlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final BitacoraNoSqlService bitacoraNoSqlService;

    public ApiExceptionHandler(BitacoraNoSqlService bitacoraNoSqlService) {
        this.bitacoraNoSqlService = bitacoraNoSqlService;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        var status = HttpStatus.valueOf(ex.getStatusCode().value());
        var message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        if (status.is5xxServerError()) {
            bitacoraNoSqlService.registrarError(message, status.value(), ex.toString(), request);
        }
        return ResponseEntity
            .status(status)
            .body(new ApiErrorResponse(status.value(), message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), "Recurso no encontrado."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        bitacoraNoSqlService.registrarError(
            ex.getMessage() == null ? "Error inesperado" : ex.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.toString(),
            request
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrio un error inesperado."
            ));
    }

    public record ApiErrorResponse(int status, String message) {
    }
}
