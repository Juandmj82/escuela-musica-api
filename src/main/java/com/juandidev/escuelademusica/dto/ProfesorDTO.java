package com.juandidev.escuelademusica.dto;

import com.juandidev.escuelademusica.enums.Modalidad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfesorDTO(
        Long id,  // No necesita validación, es generado

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden tener más de 100 caracteres")
        String apellidos,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        String email,  // La unicidad se valida en la entidad, no en el DTO

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 10, max = 10, message = "El teléfono debe tener 10 dígitos")
        String telefono,

        @NotBlank(message = "El instrumento es obligatorio")
        @Size(max = 100, message = "El instrumento no puede tener más de 100 caracteres")
        String instrumento,

        Modalidad modalidad  // No necesita @Enumerated en el DTO
) {}