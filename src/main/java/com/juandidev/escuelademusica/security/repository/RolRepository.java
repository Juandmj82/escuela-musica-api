package com.juandidev.escuelademusica.security.repository;

import com.juandidev.escuelademusica.security.model.ERol; // ¡IMPORTANTE: Asegúrate de importar ERol!
import com.juandidev.escuelademusica.security.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Asegúrate de que esta importación también esté

import java.util.Optional;

@Repository // Es buena práctica tener esta anotación
public interface RolRepository extends JpaRepository<Rol, Long> { // Cambié Integer a Long aquí, ¡verifica si tu ID de Rol es Integer o Long!

    // Método para buscar un Rol por su nombre (que ahora es de tipo ERol)
    Optional<Rol> findByNombre(ERol nombre); // <-- ¡Cambio clave aquí!
}