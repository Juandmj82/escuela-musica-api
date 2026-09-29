package com.juandidev.escuelademusica.exception;

public class ProfesorNotFoundException extends RuntimeException {
    public ProfesorNotFoundException(String mensaje){
        super(mensaje);
    }
}
