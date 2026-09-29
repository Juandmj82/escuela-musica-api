package com.juandidev.escuelademusica.security.jwt;

import com.juandidev.escuelademusica.security.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Para crear el objeto de autenticación
import org.springframework.security.core.context.SecurityContextHolder; // Para establecer el usuario en el contexto de seguridad
import org.springframework.security.core.userdetails.UserDetails; // Interfaz de detalles del usuario
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // Para detalles de autenticación web
import org.springframework.stereotype.Component; // Indica que es un componente de Spring
import org.springframework.util.StringUtils; // Utilidad para cadenas (ej. para verificar si es vacía)
import org.springframework.web.filter.OncePerRequestFilter; // Filtro base que se ejecuta una vez por petición

import java.io.IOException;

@Component // Marca esta clase como un componente de Spring para que pueda ser gestionada e inyectada.
@RequiredArgsConstructor // Lombok: Genera el constructor para inyectar JwtUtils y UserDetailsServiceImpl.
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Extiende OncePerRequestFilter para asegurar que se ejecuta una vez por petición.

    // Inyectamos nuestras utilidades JWT y nuestro servicio para cargar detalles de usuario.
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    // Logger para registrar mensajes.
    // private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Descomentar si usas logger

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Obtener el token JWT del encabezado de la petición.
            String jwt = parseJwt(request);

            // 2. Si se encontró un token JWT y es válido...
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // a. Extraer el nombre de usuario del token.
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // b. Cargar los detalles del usuario usando nuestro UserDetailsService.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // c. Crear un objeto de autenticación para Spring Security.
                //    El segundo parámetro (credenciales) se establece a null por seguridad una vez validado el token.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // Las credenciales (contraseña) no son necesarias aquí, ya se validó el token.
                                userDetails.getAuthorities()); // Obtiene los roles/permisos del usuario.

                // d. Establecer los detalles de la autenticación (ej. IP del cliente).
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // e. Establecer el objeto de autenticación en el SecurityContextHolder.
                //    Esto le dice a Spring Security que el usuario está autenticado para esta petición.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Si ocurre algún error durante el proceso de autenticación JWT.
            // logger.error("No se puede establecer la autenticación del usuario: {}", e.getMessage()); // Descomentar si usas logger
            System.out.println("No se puede establecer la autenticación del usuario: " + e.getMessage()); // Simple print para debug
        }

        // 3. Continuar con la cadena de filtros de Spring Security.
        //    Esto asegura que la petición siga su curso normal hacia el controlador.
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extraer el JWT del encabezado "Authorization".
    private String parseJwt(HttpServletRequest request) {
        // Obtiene el valor del encabezado "Authorization".
        String headerAuth = request.getHeader("Authorization");

        // Verifica si el encabezado existe y empieza con "Bearer ".
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Si es así, extrae el token (la parte después de "Bearer ").
            return headerAuth.substring(7);
        }
        return null; // Si no se encuentra un token "Bearer", devuelve null.
    }
}