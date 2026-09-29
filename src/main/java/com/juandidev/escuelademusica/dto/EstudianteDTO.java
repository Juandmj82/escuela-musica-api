package com.juandidev.escuelademusica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set; // <--- Importante: Para el Set de IDs de cursos

public record EstudianteDTO(

        Long id,

        @NotBlank(message = "El primer nombre es obligatorio")
        @Size(max = 100, message = "El primer nombre no puede exceder 100 caracteres")
        String primerNombre,

        @Size(max = 100, message = "El segundo nombre no puede exceder 100 caracteres")
        String segundoNombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Email debe tener formato correcto")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser pasada")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La nacionalidad es obligatoria")
        @Size(max = 50, message = "La nacionalidad no puede exceder 50 caracteres")
        String nacionalidad,

        // --- ESTE ES EL CAMPO EN CUESTIÓN ---
        Set<Long> cursosIds // <--- ¡Aquí está!
) {}