package com.juandidev.escuelademusica.repository;

import com.juandidev.escuelademusica.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudianteRepository  extends JpaRepository<Estudiante, Long> {
}
