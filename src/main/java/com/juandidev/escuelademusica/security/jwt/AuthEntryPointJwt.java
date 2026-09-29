package com.juandidev.escuelademusica.security.jwt;

import jakarta.servlet.ServletException; // Excepción de Servlet
import jakarta.servlet.http.HttpServletRequest; // Petición HTTP
import jakarta.servlet.http.HttpServletResponse; // Respuesta HTTP
import org.slf4j.Logger; // Para loggear errores
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component; // Componente de Spring

import java.io.IOException;

@Component // Marca esta clase como un componente de Spring.
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    // Logger para registrar mensajes de error.
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    // Este método se invoca cuando un usuario no autenticado intenta acceder a un recurso protegido.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Registra el error de autenticación.
        logger.error("Error no autorizado: {}", authException.getMessage());

        // Establece el código de estado HTTP a 401 (Unauthorized - No Autorizado).
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: No autorizado");
    }
}