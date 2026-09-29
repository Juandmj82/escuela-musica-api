package com.juandidev.escuelademusica.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token; // El token JWT generado.
    private String type = "Bearer"; // Tipo de token (por convención, "Bearer").
    private Long id;
    private String username;
    private List<String> roles;

    // Constructor para inicializar el objeto JwtResponse.
    public JwtResponse(String accessToken, Long id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}