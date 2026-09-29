package com.juandidev.escuelademusica.security.model;

// Define los posibles roles que un usuario puede tener en la aplicación.
// Estos nombres deben coincidir con los nombres de los roles en tu base de datos
// (ej. en la tabla 'roles', la columna 'nombre').
public enum ERol {
    ROLE_USER, // Rol para usuarios estándar
    ROLE_ADMIN // Rol para administradores con privilegios completos
}