package com.juandidev.escuelademusica.exception;

public class CursoNotFoundException extends RuntimeException {
    public CursoNotFoundException(String mensaje){
        super(mensaje);

    }
}
