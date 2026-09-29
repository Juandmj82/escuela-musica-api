package com.juandidev.escuelademusica.security.controller;

import com.juandidev.escuelademusica.payload.request.RegisterRequest;
import com.juandidev.escuelademusica.payload.response.MessageResponse;
import com.juandidev.escuelademusica.security.model.ERol;
import com.juandidev.escuelademusica.security.model.Rol;
import com.juandidev.escuelademusica.security.model.Usuario;
import com.juandidev.escuelademusica.security.repository.RolRepository;
import com.juandidev.escuelademusica.security.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600) // Asegúrate de que esto sea adecuado para tu entorno de prod
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden listar usuarios
    public ResponseEntity<List<Usuario>> getAllUsers() {
        // Por seguridad, considera devolver un DTO más ligero que no exponga la contraseña
        // Por simplicidad, aquí devolvemos la entidad Usuario completa.
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // GET /api/users/{id} - Obtener un usuario por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden ver usuarios por ID
    public ResponseEntity<Usuario> getUserById(@PathVariable Long id) {
        // Por seguridad, considera devolver un DTO más ligero
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con ID " + id));
        return ResponseEntity.ok(usuario);
    }

    // POST /api/users - Crear un nuevo usuario (solo para ADMINs)
    // Reutilizamos RegisterRequest, pero aquí el admin puede especificar roles
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden crear usuarios (con roles específicos)
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (usuarioRepository.existsByNombreUsuario(registerRequest.getNombreUsuario())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El nombre de usuario ya está en uso!"));
        }

        Usuario usuario = new Usuario(registerRequest.getNombreUsuario(),
                passwordEncoder.encode(registerRequest.getContrasena()));

        Set<Rol> roles = new HashSet<>();
        // Asignación de roles basada en lo que el administrador envía
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            // Si no se especifican roles, por defecto asignamos ROLE_USER
            Rol userRol = rolRepository.findByNombre(ERol.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Rol 'ROLE_USER' no encontrado."));
            roles.add(userRol);
        } else {
            registerRequest.getRoles().forEach(rolString -> {
                ERol eRol = ERol.valueOf(rolString.toUpperCase()); // Convierte String a Enum
                Rol rol = rolRepository.findByNombre(eRol)
                        .orElseThrow(() -> new RuntimeException("Error: Rol '" + rolString + "' no encontrado."));
                roles.add(rol);
            });
        }
        usuario.setRoles(roles);
        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Usuario creado con éxito!"));
    }

    // PUT /api/users/{id} - Actualizar un usuario existente (solo para ADMINs)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden actualizar usuarios
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody RegisterRequest updateRequest) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado con ID " + id));

        // Verificar si el nombre de usuario ha cambiado y si el nuevo nombre ya existe
        if (!usuario.getNombreUsuario().equals(updateRequest.getNombreUsuario()) &&
                usuarioRepository.existsByNombreUsuario(updateRequest.getNombreUsuario())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El nuevo nombre de usuario ya está en uso!"));
        }

        usuario.setNombreUsuario(updateRequest.getNombreUsuario());

        // Solo actualizar contraseña si se proporciona una nueva
        if (updateRequest.getContrasena() != null && !updateRequest.getContrasena().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(updateRequest.getContrasena()));
        }

        // Actualizar roles
        Set<Rol> newRoles = new HashSet<>();
        if (updateRequest.getRoles() == null || updateRequest.getRoles().isEmpty()) {
            // Si se eliminan todos los roles o no se especifican, asigna ROLE_USER por defecto
            Rol userRol = rolRepository.findByNombre(ERol.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Rol 'ROLE_USER' no encontrado."));
            newRoles.add(userRol);
        } else {
            updateRequest.getRoles().forEach(rolString -> {
                ERol eRol = ERol.valueOf(rolString.toUpperCase());
                Rol rol = rolRepository.findByNombre(eRol)
                        .orElseThrow(() -> new RuntimeException("Error: Rol '" + rolString + "' no encontrado."));
                newRoles.add(rol);
            });
        }
        usuario.setRoles(newRoles);

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new MessageResponse("Usuario actualizado con éxito!"));
    }

    // DELETE /api/users/{id} - Eliminar un usuario (solo para ADMINs)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden eliminar usuarios
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: Usuario no encontrado con ID " + id));
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Usuario eliminado con éxito!"));
    }
}