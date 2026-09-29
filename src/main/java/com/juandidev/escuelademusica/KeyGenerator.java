package com.juandidev.escuelademusica;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Genera una clave segura para HS512
        byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        System.out.println("Generated HS512 Key (Base64 encoded): " + base64Key);
        // Salida de ejemplo: Generated HS512 Key (Base64 encoded): l8G8S... (una cadena muy larga)
    }
}