package com.juandidev.escuelademusica.model;

import com.juandidev.escuelademusica.enums.Modalidad;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cursos")
// Excluye ambas colecciones del toString para evitar LazyInitializationException y StackOverflowError
@ToString(exclude = {"profesores", "estudiantes"})
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede tener más de 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    // Relación ManyToMany con Profesor (Tu implementación actual)
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "curso_profesor", // Nombre de la tabla de unión para profesores
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "profesor_id")
    )
    private Set<Profesor> profesores = new HashSet<>();

    // --- NUEVA RELACIÓN MANY-TO-MANY CON ESTUDIANTE ---
    // Esta es la parte "inversa" de la relación con Estudiante.
    // La entidad "dueña" de la relación es Estudiante (donde usamos @JoinTable).
    // Usamos 'mappedBy' aquí para indicar el campo en la entidad 'Estudiante' que es el dueño.
    // 'cursos' es el nombre del Set<Curso> en Estudiante.java.
    @ManyToMany(mappedBy = "cursos", fetch = FetchType.LAZY)
    private Set<Estudiante> estudiantes = new HashSet<>(); // Inicializar para evitar NullPointerException

    // Métodos de ayuda para gestionar la relación bidireccional con Profesor
    public void agregarProfesor(Profesor profesor) {
        if (this.profesores == null) {
            this.profesores = new HashSet<>();
        }
        this.profesores.add(profesor);
        if (profesor.getCursos() == null) {
            profesor.setCursos(new HashSet<>());
        }
        profesor.getCursos().add(this);
    }

    public void eliminarProfesor(Profesor profesor) {
        if (this.profesores != null) {
            this.profesores.remove(profesor);
        }
        if (profesor.getCursos() != null) {
            profesor.getCursos().remove(this);
        }
    }

    // --- NUEVOS Métodos de ayuda para gestionar la relación bidireccional con Estudiante ---
    public void agregarEstudiante(Estudiante estudiante) {
        if (this.estudiantes == null) {
            this.estudiantes = new HashSet<>();
        }
        this.estudiantes.add(estudiante);
        if (estudiante.getCursos() != null && !estudiante.getCursos().contains(this)) {
            estudiante.getCursos().add(this);
        }
    }

    public void eliminarEstudiante(Estudiante estudiante) {
        if (this.estudiantes != null) {
            this.estudiantes.remove(estudiante);
        }
        // Asegúrate de que Estudiante.java tenga un método eliminarCurso
        if (estudiante.getCursos() != null) {
            estudiante.getCursos().remove(this);
        }
    }
}