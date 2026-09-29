package com.juandidev.escuelademusica.security.controller;

import com.juandidev.escuelademusica.payload.request.LoginRequest;
import com.juandidev.escuelademusica.payload.request.RegisterRequest;
import com.juandidev.escuelademusica.payload.response.JwtResponse;
import com.juandidev.escuelademusica.payload.response.MessageResponse;
import com.juandidev.escuelademusica.security.jwt.JwtUtils;
import com.juandidev.escuelademusica.security.model.ERol;
import com.juandidev.escuelademusica.security.model.Rol;
import com.juandidev.escuelademusica.security.model.Usuario;
import com.juandidev.escuelademusica.security.repository.RolRepository;
import com.juandidev.escuelademusica.security.repository.UsuarioRepository;
import com.juandidev.escuelademusica.security.service.impl.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * Endpoint para el registro de nuevos usuarios.
     * Por seguridad, cualquier registro público asignará siempre el rol ROLE_USER.
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Verifica si el nombre de usuario ya está en uso
        if (usuarioRepository.existsByNombreUsuario(registerRequest.getNombreUsuario())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ¡El nombre de usuario ya está en uso!"));
        }

        // Crea un nuevo objeto Usuario con el nombre de usuario y la contraseña encriptada
        Usuario usuario = new Usuario(registerRequest.getNombreUsuario(),
                passwordEncoder.encode(registerRequest.getContrasena()));

        Set<Rol> roles = new HashSet<>();

        // *** LÓGICA DE ASIGNACIÓN DE ROL: Siempre asignamos ROLE_USER para registros públicos ***
        // Busca el rol ROLE_USER en la base de datos
        Rol userRol = rolRepository.findByNombre(ERol.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Rol 'ROLE_USER' no encontrado en la base de datos."));

        roles.add(userRol); // Añade el rol ROLE_USER al conjunto de roles

        usuario.setRoles(roles);

        // Guarda el nuevo usuario en la base de datos
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado con éxito!"));
    }

    /**
     * Endpoint para el inicio de sesión de usuarios.
     * Autentica al usuario y devuelve un token JWT con sus roles.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. Autenticar al usuario usando el AuthenticationManager de Spring Security.
        // Se crea un token con el nombre de usuario y la contraseña proporcionados.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getNombreUsuario(), loginRequest.getContrasena()));

        // 2. Establecer la autenticación en el contexto de seguridad de Spring.
        // Esto es importante para que Spring Security reconozca al usuario en la petición actual.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generar el token JWT para el usuario autenticado.
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 4. Obtener los detalles completos del usuario autenticado (UserDetailsImpl).
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 5. Extraer los roles del usuario (GrantedAuthority) y convertirlos a una lista de Strings.
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // El método getAuthority() devuelve el nombre del rol (ej. "ROLE_USER")
                .collect(Collectors.toList());

        // 6. Devolver la respuesta JWT con el token, ID del usuario, nombre de usuario y sus roles.
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }
}