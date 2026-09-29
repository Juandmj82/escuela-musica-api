package com.juandidev.escuelademusica.repository;

import com.juandidev.escuelademusica.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
}
