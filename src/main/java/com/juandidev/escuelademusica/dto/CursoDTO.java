package com.juandidev.escuelademusica.dto;

import com.juandidev.escuelademusica.enums.Modalidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CursoDTO(
        Long id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede tener más de 500 caracteres")
        String descripcion,

        @NotNull(message = "La modalidad es obligatoria")
        Modalidad modalidad,

        Set<Long> profesoresIds // <--- ¡Aquí está!
) {}