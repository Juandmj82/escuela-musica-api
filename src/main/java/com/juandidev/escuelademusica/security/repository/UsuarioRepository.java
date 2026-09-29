package com.juandidev.escuelademusica.security.repository;

import com.juandidev.escuelademusica.security.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Este método nos permitirá verificar si un nombre de usuario ya existe en la base de datos.
    Boolean existsByNombreUsuario(String nombreUsuario);
}
