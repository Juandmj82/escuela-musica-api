package com.juandidev.escuelademusica.security.jwt;

import com.juandidev.escuelademusica.security.service.impl.UserDetailsImpl; // Importa nuestra implementación de UserDetails
import io.jsonwebtoken.*; // Importa todas las clases JWT de jjwt
import io.jsonwebtoken.io.Decoders; // Para decodificar la clave secreta
import io.jsonwebtoken.security.Keys; // Para generar claves seguras
import org.slf4j.Logger; // Para loggear mensajes (errores, etc.)
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Para inyectar valores desde application.properties
import org.springframework.security.core.Authentication; // Objeto de autenticación de Spring Security
import org.springframework.security.core.GrantedAuthority; // Para manejar los roles/autoridades
import org.springframework.stereotype.Component; // Indica que es un componente de Spring

import java.security.Key; // Interfaz para claves de seguridad
import java.util.Date;    // Para manejar fechas de expiración
import java.util.List;    // Para la lista de roles
import java.util.stream.Collectors; // Para procesar streams

@Component // Marca esta clase como un componente de Spring para que pueda ser inyectada en otros beans.
public class JwtUtils {

    // Logger para registrar eventos y errores.
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Secreta de JWT: clave para firmar y verificar tokens. Se inyecta desde application.properties.
    // ¡Debe ser larga y compleja en un entorno de producción!
    @Value("${escuelademusica.app.jwtSecret}") // Asegúrate que esta propiedad exista en application.properties
    private String jwtSecret;

    // Tiempo de expiración del JWT en milisegundos. Se inyecta desde application.properties.
    @Value("${escuelademusica.app.jwtExpirationMs}") // Asegúrate que esta propiedad exista en application.properties
    private int jwtExpirationMs;

    // --- Métodos para obtener la clave de firma ---
    // Decodifica la clave secreta (String) a un objeto Key seguro.
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // --- Métodos para generar un JWT ---
    // Genera un JWT para un usuario autenticado.
    public String generateJwtToken(Authentication authentication) {
        // Obtiene los detalles del usuario autenticado (nuestro UserDetailsImpl).
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // **NUEVO: Obtener los roles/autoridades del usuario**
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // Mapea cada autoridad a su String (ej. "ROLE_ADMIN")
                .collect(Collectors.toList());      // Recolecta en una lista

        // Construye el JWT:
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername())) // El "subject" del token es el nombre de usuario.
                .claim("roles", roles) // <-- ¡AQUÍ SE AÑADEN LOS ROLES COMO UN CLAIM!
                .setIssuedAt(new Date()) // Fecha de emisión del token.
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Fecha de expiración.
                .signWith(key(), SignatureAlgorithm.HS512) // Firma el token con nuestra clave secreta y algoritmo HS512.
                .compact(); // Compacta el JWT en su formato de cadena final.
    }

    // --- Métodos para validar un JWT ---
    // Valida si un token JWT es válido y no ha expirado.
    public boolean validateJwtToken(String authToken) {
        try {
            // Intenta parsear y verificar el token con nuestra clave.
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken); // Usar parse() en lugar de parseClaimsJws() si solo se necesita validar
            return true; // Si no hay excepciones, el token es válido.
        } catch (MalformedJwtException e) {
            logger.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("La cadena JWT está vacía: {}", e.getMessage());
        }
        return false; // Si ocurre alguna excepción, el token es inválido.
    }

    // --- Métodos para extraer información de un JWT ---
    // Obtiene el nombre de usuario del token JWT.
    public String getUserNameFromJwtToken(String authToken) {
        // Parsea el token y obtiene los "claims" (información) del body del JWT.
        // Luego, obtiene el "subject", que es nuestro nombre de usuario.
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody().getSubject();
    }
}