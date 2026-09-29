package com.juandidev.escuelademusica.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor; // Este genera el constructor con todos los campos
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name= "usuarios")
@Getter
@Setter
@NoArgsConstructor // Genera el constructor sin argumentos
@AllArgsConstructor // Genera el constructor con TODOS los argumentos (id, nombreUsuario, contrasena, roles)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombreUsuario;

    @Column(nullable = false)
    private String contrasena;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuarios_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    // Constructor para crear un nuevo usuario con nombre de usuario y contraseña
    public Usuario(String nombreUsuario, String contrasena) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.roles = new HashSet<>(); // Asegura que la colección de roles se inicialice
    }

    public void agregarRol(Rol rol){
        this.roles.add(rol);
    }

    public void eliminarRol(Rol rol){
        this.roles.remove(rol);
    }
}