package com.juandidev.escuelademusica.model;

import com.juandidev.escuelademusica.enums.Modalidad;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name ="profesores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"cursos"}) // <--- EXCLUIMOS la colección 'cursos' de toString
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres") // Añadido para consistencia
    @Column(nullable = false, length = 100) // Añadido para consistencia
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres") // Añadido para consistencia
    @Column(nullable = false, length = 100) // Añadido para consistencia
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener formato correcto")
    @Column(nullable = false, unique = true, length = 100) // Añadido longitud
    private String email;

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(min = 10, max = 10, message = "El número debe tener 10 digitos")
    @Column(length = 10)
    private String telefono;

    @NotBlank(message = "El instrumento a dictar es obligatorio")
    @Size(max = 50, message = "El instrumento no puede exceder 50 caracteres") // Añadido para consistencia
    @Column(nullable = false, length = 50) // Añadido longitud
    private String instrumento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) // Asegúrate de tener una longitud para VARCHAR
    private Modalidad modalidad;

    // Relación ManyToMany con Curso (esta es la parte inversa)
    // 'mappedBy = "profesores"' indica que el campo 'profesores' en la entidad Curso
    // es el dueño de esta relación.
    @ManyToMany(mappedBy = "profesores", fetch = FetchType.LAZY) // <--- Añadido FetchType.LAZY por buena práctica
    private Set<Curso> cursos = new HashSet<>(); // <--- ¡IMPORTANTE! Inicializar la colección aquí

    // Métodos de ayuda para gestionar la relación bidireccional (opcional, pero buena práctica)
    public void agregarCurso(Curso curso) {
        if (this.cursos == null) {
            this.cursos = new HashSet<>();
        }
        this.cursos.add(curso);
        // Asegura la bidireccionalidad en el lado del Curso si Curso es el dueño
        if (curso.getProfesores() != null && !curso.getProfesores().contains(this)) {
            curso.getProfesores().add(this);
        }
    }

    public void eliminarCurso(Curso curso) {
        if (this.cursos != null) {
            this.cursos.remove(curso);
        }
        // Asegura la bidireccionalidad en el lado del Curso
        if (curso.getProfesores() != null) {
            curso.getProfesores().remove(this);
        }
    }
}