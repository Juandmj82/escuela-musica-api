package com.juandidev.escuelademusica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size; // Nueva importación para @Size
import lombok.AllArgsConstructor;
import lombok.Getter; // Nuevo
import lombok.NoArgsConstructor;
import lombok.Setter; // Nuevo
import lombok.ToString; // Nuevo
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet; // Nuevo
import java.util.Set;     // Nuevo

@Getter // Usamos @Getter
@Setter // Usamos @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estudiantes")
// Excluye 'cursos' del toString para evitar LazyInitializationException y recursión infinita.
@ToString(exclude = {"cursos"}) // <--- IMPORTANTE: Excluir relaciones bidireccional
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 100, message = "El primer nombre no puede exceder 100 caracteres") // Añadiendo Size para consistencia
    @Column(nullable = false, length = 100)
    private String primerNombre;

    @Size(max = 100, message = "El segundo nombre no puede exceder 100 caracteres") // Añadiendo Size
    @Column(length = 100)
    private String segundoNombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres") // Añadiendo Size
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe tener formato correcto")
    @Column(nullable = false, unique = true, length = 100) // Asegúrate de que tenga longitud
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Debe ser fecha pasada")
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Size(max = 50, message = "La nacionalidad no puede exceder 50 caracteres") // Añadiendo Size
    @Column(nullable = false, length = 50)
    private String nacionalidad;

    // --- NUEVA RELACIÓN MANY-TO-MANY CON CURSO ---
    // Un estudiante puede estar inscrito en múltiples cursos, y un curso puede tener múltiples estudiantes.
    // Esta es la parte "dueña" de la relación, por lo tanto, usa @JoinTable
    @ManyToMany(fetch = FetchType.LAZY) // Carga perezosa de los cursos
    @JoinTable(
            name = "estudiantes_cursos", // Nombre de la tabla de unión (intermedia)
            joinColumns = @JoinColumn(name = "estudiante_id"), // Columna en 'estudiantes_cursos' que hace referencia a 'estudiantes'
            inverseJoinColumns = @JoinColumn(name = "curso_id") // Columna en 'estudiantes_cursos' que hace referencia a 'cursos'
    )
    private Set<Curso> cursos = new HashSet<>(); // Inicializar para evitar NullPointerException

    // --- Método getter explícito para la colección 'cursos' (añadido para MapStruct) ---
    public Set<Curso> getCursos() {
        return this.cursos;
    }

    // --- Métodos de ayuda para gestionar la relación (Opcional, pero muy recomendado) ---
    public void agregarCurso(Curso curso) {
        if (this.cursos == null) {
            this.cursos = new HashSet<>();
        }
        this.cursos.add(curso);
        // Si la relación en Curso.java es bidireccional y usa mappedBy="estudiantes",
        // entonces también debes actualizar el lado del Curso:
        if (curso.getEstudiantes() != null && !curso.getEstudiantes().contains(this)) {
            curso.getEstudiantes().add(this);
        }
    }

    public void eliminarCurso(Curso curso) {
        if (this.cursos != null) {
            this.cursos.remove(curso);
        }
        // Si la relación en Curso.java es bidireccional y usa mappedBy="estudiantes",
        // entonces también debes actualizar el lado del Curso:
        if (curso.getEstudiantes() != null) {
            curso.getEstudiantes().remove(this);
        }
    }
}