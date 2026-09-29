package com.juandidev.escuelademusica.security.service.impl;

import com.juandidev.escuelademusica.security.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects; // Necesario para el método equals
import java.util.stream.Collectors;


@Getter
public class UserDetailsImpl implements UserDetails {

    private Long id; // ID del usuario, útil para operaciones post-autenticación
    private String nombreUsuario;
    private String contrasena;

    // Esta colección guarda los roles del usuario como autoridades que Spring Security puede entender
    private Collection<? extends GrantedAuthority> authorities;

    // Constructor que Lombok genera con @AllArgsConstructor
    public UserDetailsImpl(Long id, String nombreUsuario, String contrasena,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.authorities = authorities;
    }

    // --- Método de Fábrica para construir UserDetailsImpl desde tu entidad Usuario ---
    // Este método estático es muy útil: toma tu objeto 'Usuario' de la base de datos
    // y lo convierte en el formato 'UserDetailsImpl' que Spring Security necesita.
    public static UserDetailsImpl build(Usuario usuario) {
        // Mapeamos los roles que tu entidad 'Usuario' tiene a una lista de 'GrantedAuthority'.
        // Cada 'Rol' se convierte en un 'SimpleGrantedAuthority' con el nombre del rol (ej., "ROLE_ADMIN").
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                // rol.getNombre() devuelve un ERol. Necesitamos su nombre como String, por eso el .name().
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre().name()))
                .collect(Collectors.toList()); // Recopila todos los roles en una lista

        // Devuelve una nueva instancia de UserDetailsImpl con los datos del Usuario y sus autoridades.
        return new UserDetailsImpl(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getContrasena(),
                authorities);
    }

    // --- Implementaciones de los Métodos de la Interfaz UserDetails ---
    // Spring Security llama a estos métodos para obtener la información necesaria del usuario.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Devuelve la lista de roles/permisos del usuario
    }

    @Override
    public String getPassword() {
        return contrasena; // Devuelve la contraseña del usuario
    }

    @Override
    public String getUsername() {
        return nombreUsuario; // Devuelve el nombre de usuario
    }

    // Estos métodos controlan el estado de la cuenta. Por ahora, los dejamos en 'true'
    // porque simplificamos la entidad Usuario. En el futuro, podrías conectarlos
    // a campos booleanos en tu entidad Usuario (si los añades).
    @Override
    public boolean isAccountNonExpired() {
        return true; // La cuenta nunca expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // La cuenta nunca está bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Las credenciales (contraseña) nunca expiran
    }

    @Override
    public boolean isEnabled() {
        return true; // La cuenta siempre está habilitada
    }

    // Un método equals y hashCode es importante para Spring Security
    // para comparar objetos UserDetails de forma fiable.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}