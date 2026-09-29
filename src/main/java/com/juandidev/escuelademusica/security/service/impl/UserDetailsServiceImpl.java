package com.juandidev.escuelademusica.security.service.impl;

import com.juandidev.escuelademusica.security.model.Usuario;
import com.juandidev.escuelademusica.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService { // Implementa la interfaz UserDetailsService de Spring Security. Esta interfaz es clave para que Spring Security sepa cómo buscar usuarios para la autenticación.

    // Inyectamos nuestro UsuarioRepository. Spring se encargará de proporcionar una instancia de este repositorio.
    private final UsuarioRepository usuarioRepository;

    // Este es el método principal de la interfaz UserDetailsService.
    // Spring Security lo llamará automáticamente cuando necesite cargar los detalles de un usuario
    // (por ejemplo, durante un intento de inicio de sesión) basándose en su nombre de usuario.
    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        // 1. Buscamos el usuario en la base de datos utilizando el 'nombreUsuario' proporcionado.
        //    'orElseThrow' significa que si el usuario no se encuentra (el Optional está vacío),
        //    se lanzará una 'UsernameNotFoundException', indicando un fallo en la autenticación.
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + nombreUsuario));

        // 2. Si el usuario se encuentra, utilizamos el método estático 'build' de nuestra clase UserDetailsImpl.
        //    Este método transforma nuestra entidad 'Usuario' (que viene de la base de datos)
        //    en un objeto 'UserDetails' que Spring Security puede entender y usar para verificar la contraseña
        //    y determinar los roles/permisos del usuario.
        return UserDetailsImpl.build(usuario);
    }
}