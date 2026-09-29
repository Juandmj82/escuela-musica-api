package com.juandidev.escuelademusica.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// No necesitamos @AllArgsConstructor si vamos a definir un constructor manual para el nombre del rol
// y un constructor vacío para JPA.

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor // Genera un constructor sin argumentos, requerido por JPA
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private ERol nombre;

    // Este es el constructor que DataLoader necesita.
    // Al quitar @AllArgsConstructor, este constructor manual no entra en conflicto.
    public Rol(ERol nombre) {
        this.nombre = nombre;
    }

    // Si aún necesitaras un constructor con 'id' y 'nombre' (aunque raro en la creación):
    // public Rol(Long id, ERol nombre) {
    //     this.id = id;
    //     this.nombre = nombre;
    // }
}