package com.juandidev.escuelademusica.service;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.dto.ProfesorDTO;
import com.juandidev.escuelademusica.enums.Modalidad;

import java.util.List;
import java.util.Set;

public interface ICursoService {

    CursoDTO crearCurso(CursoDTO dto);

    List<CursoDTO> listarCursos();

    CursoDTO buscarPorId(Long id);

    CursoDTO actualizaCurso(Long id, CursoDTO cursoDTO);

    void eliminarPorId(Long id);

    //Métodos para la relación con Profesor
    CursoDTO agregarProfesorACurso(Long cursoId, Long profesorId);
    void eliminarProfesorDeCurso(Long cursoId, Long profesorId);
    Set<ProfesorDTO> listarProfesoresDelCurso(Long id);

    //métodos de búsqueda
    List<CursoDTO> buscarPorModalidad(Modalidad modalidad);
    List<CursoDTO> buscarPorNombreContaining(String nombre);




}
