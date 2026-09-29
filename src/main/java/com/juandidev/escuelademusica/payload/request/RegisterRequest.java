package com.juandidev.escuelademusica.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String nombreUsuario;

    @NotBlank
    @Size(min = 6, max = 40)
    private String contrasena;

    private Set<String> roles;
}