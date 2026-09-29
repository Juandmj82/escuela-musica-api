package com.juandidev.escuelademusica; // Tu paquete DataLoader

import com.juandidev.escuelademusica.security.model.ERol; // ¡IMPORTA EL ENUM ERol!
import com.juandidev.escuelademusica.security.model.Rol;
import com.juandidev.escuelademusica.security.model.Usuario; // Si también estás inicializando usuarios
import com.juandidev.escuelademusica.security.repository.RolRepository;
import com.juandidev.escuelademusica.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Si encriptas contraseñas aquí

import java.util.HashSet;
import java.util.Set;

@Configuration // O @Component, o si está en tu clase principal @SpringBootApplication con @Bean
@RequiredArgsConstructor
public class DataLoader {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository; // Si lo tienes
    private final PasswordEncoder passwordEncoder; // Si lo tienes

    @Bean
    public CommandLineRunner initRolesAndAdminUser() {
        return args -> {
            // --- Inicialización de ROLES ---
            Rol userRol = null;
            if (rolRepository.findByNombre(ERol.ROLE_USER).isEmpty()) {
                userRol = rolRepository.save(new Rol(ERol.ROLE_USER)); // <-- ¡CORRECCIÓN AQUÍ!
                System.out.println("Rol ROLE_USER creado.");
            } else {
                userRol = rolRepository.findByNombre(ERol.ROLE_USER).get();
            }

            Rol adminRol = null;
            if (rolRepository.findByNombre(ERol.ROLE_ADMIN).isEmpty()) {
                adminRol = rolRepository.save(new Rol(ERol.ROLE_ADMIN)); // <-- ¡CORRECCIÓN AQUÍ!
                System.out.println("Rol ROLE_ADMIN creado.");
            } else {
                adminRol = rolRepository.findByNombre(ERol.ROLE_ADMIN).get();
            }

            // --- Inicialización de USUARIO ADMINISTRADOR (opcional) ---
            // Solo si quieres un usuario admin predefinido al inicio
            if (usuarioRepository != null && usuarioRepository.findByNombreUsuario("admin@example.com").isEmpty()) {
                Usuario adminUser = new Usuario("admin@example.com", passwordEncoder.encode("adminpassword")); // Contraseña para el admin
                Set<Rol> adminRoles = new HashSet<>();
                adminRoles.add(adminRol); // Asegúrate de asignar el rol de ADMIN
                adminUser.setRoles(adminRoles);
                usuarioRepository.save(adminUser);
                System.out.println("Usuario 'admin@example.com' creado con rol ADMIN.");
            } else if (usuarioRepository != null) {
                System.out.println("Usuario 'admin@example.com' ya existe.");
            }

            // Si también estabas creando un usuario normal predefinido, asegúrate de asignarle userRol
            if (usuarioRepository != null && usuarioRepository.findByNombreUsuario("user@example.com").isEmpty()) {
                Usuario normalUser = new Usuario("user@example.com", passwordEncoder.encode("userpassword")); // Contraseña para el usuario
                Set<Rol> normalUserRoles = new HashSet<>();
                normalUserRoles.add(userRol); // Asegúrate de asignar el rol de USER
                normalUser.setRoles(normalUserRoles);
                usuarioRepository.save(normalUser);
                System.out.println("Usuario 'user@example.com' creado con rol USER.");
            } else if (usuarioRepository != null) {
                System.out.println("Usuario 'user@example.com' ya existe.");
            }

        };
    }
}