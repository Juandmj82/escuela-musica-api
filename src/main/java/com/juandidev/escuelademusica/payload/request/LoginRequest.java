package com.juandidev.escuelademusica.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank
    private String nombreUsuario;

    @NotBlank
    private String contrasena;


}