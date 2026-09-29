package com.juandidev.escuelademusica.service;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.dto.EstudianteDTO; // ¡Asegúrate de que esta sea la ÚNICA importación de DTO de Estudiante!

import java.util.List;
import java.util.Set;

public interface IEstudianteService {

    EstudianteDTO crearEstudiante(EstudianteDTO estudianteDTO);

    // MÉTODOS CORREGIDOS: Ahora devuelven EstudianteDTO
    List<EstudianteDTO> listarEstudiantes(); // <--- CORREGIDO a EstudianteDTO
    EstudianteDTO buscarPorId(Long id);     // <--- CORREGIDO a EstudianteDTO

    EstudianteDTO actualizarEstudiante(Long id, EstudianteDTO estudianteDTO);
    void eliminarPorId(Long id);

    EstudianteDTO inscribirEstudianteEnCurso(Long estudianteId, Long cursoId);
    EstudianteDTO desinscribirEstudianteDeCurso(Long estudianteId, Long cursoId);

    Set<CursoDTO> listarCursosInscritos(Long estudianteId);
}