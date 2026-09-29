package com.juandidev.escuelademusica.repository;

import com.juandidev.escuelademusica.enums.Modalidad;
import com.juandidev.escuelademusica.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CursoRepository  extends JpaRepository<Curso, Long> {
    List<Curso> findByModalidad(Modalidad modalidad);
    List<Curso> findByNombreContainingIgnoreCase(String nombre);



}
