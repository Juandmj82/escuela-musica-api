package com.juandidev.escuelademusica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EstudianteNotFoundException.class)
    public ResponseEntity<String> handlerEstudianteNotFoundException(EstudianteNotFoundException ex){
        String mensajeDeError = ex.getMessage();

        return new ResponseEntity<>(mensajeDeError,HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Esto establece el estado HTTP para este tipo de excepción
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())); // Mapea el campo al mensaje de error
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProfesorNotFoundException.class)
    public ResponseEntity<String> handlerProfesorNotFoundException(ProfesorNotFoundException ex){
        String mensajeError = ex.getMessage();

        return new ResponseEntity<>(mensajeError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CursoNotFoundException.class)
    public ResponseEntity<String> handlerCursoNotFoundException(CursoNotFoundException ex){
        String mensajeError = ex.getMessage();

        return new ResponseEntity<>(mensajeError,HttpStatus.NOT_FOUND);
    }
}
