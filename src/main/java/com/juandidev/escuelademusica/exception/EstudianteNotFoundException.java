package com.juandidev.escuelademusica.exception;

public class EstudianteNotFoundException extends RuntimeException{
    public EstudianteNotFoundException(String mensaje){
        super(mensaje);

    }
}
