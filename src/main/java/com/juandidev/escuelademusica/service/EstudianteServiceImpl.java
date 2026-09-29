package com.juandidev.escuelademusica.service;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.dto.EstudianteDTO;
import com.juandidev.escuelademusica.exception.CursoNotFoundException;
import com.juandidev.escuelademusica.exception.EstudianteNotFoundException;
import com.juandidev.escuelademusica.mapper.CursoMapper;
import com.juandidev.escuelademusica.mapper.EstudianteMapper;
import com.juandidev.escuelademusica.model.Curso;
import com.juandidev.escuelademusica.model.Estudiante;
import com.juandidev.escuelademusica.repository.CursoRepository;
import com.juandidev.escuelademusica.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements IEstudianteService { // Asumiendo que IEstudianteService existe y es correcto

    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository; // Inyectar CursoRepository
    private final EstudianteMapper estudianteMapper;
    private final CursoMapper cursoMapper;

    @Override
    @Transactional // Asegura que la operación sea transaccional
    public EstudianteDTO crearEstudiante(EstudianteDTO estudianteDTO) {
        Estudiante estudiante = estudianteMapper.toEntity(estudianteDTO);

        // Manejar los cursosIds al crear el estudiante
        if (estudianteDTO.cursosIds() != null && !estudianteDTO.cursosIds().isEmpty()) {
            Set<Curso> cursos = new HashSet<>();
            for (Long cursoId : estudianteDTO.cursosIds()) {
                Curso curso = cursoRepository.findById(cursoId)
                        .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + cursoId));
                cursos.add(curso);
                // Asegurar la bidireccionalidad si es necesario desde este lado
                curso.agregarEstudiante(estudiante); // Asumiendo que Curso tiene este método
            }
            estudiante.setCursos(cursos);
        }

        Estudiante guardado = estudianteRepository.save(estudiante);
        return estudianteMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteDTO> listarEstudiantes() { // Modificado para devolver EstudianteDTO
        return estudianteRepository.findAll().stream()
                .map(estudianteMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDTO buscarPorId(Long id) { // Modificado para devolver EstudianteDTO
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNotFoundException("Estudiante no encontrado con ID: " + id));
        return estudianteMapper.toDTO(estudiante);
    }

    @Override
    @Transactional // Asegura que la operación sea transaccional
    public EstudianteDTO actualizarEstudiante(Long id, EstudianteDTO estudianteDTO) {
        Estudiante existente = estudianteRepository.findById(id)
                .orElseThrow(() -> new EstudianteNotFoundException("Estudiante no encontrado con ID: " + id));

        // Actualizar campos básicos
        existente.setPrimerNombre(estudianteDTO.primerNombre());
        existente.setSegundoNombre(estudianteDTO.segundoNombre());
        existente.setApellidos(estudianteDTO.apellidos());
        existente.setEmail(estudianteDTO.email());
        existente.setFechaNacimiento(estudianteDTO.fechaNacimiento());
        existente.setNacionalidad(estudianteDTO.nacionalidad());

        // Manejar actualización de cursos: limpia los existentes y añade los nuevos
        existente.getCursos().clear(); // Limpia los cursos actuales

        if (estudianteDTO.cursosIds() != null && !estudianteDTO.cursosIds().isEmpty()) {
            for (Long cursoId : estudianteDTO.cursosIds()) {
                Curso curso = cursoRepository.findById(cursoId)
                        .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + cursoId));
                existente.agregarCurso(curso); // Usa el método helper de la entidad Estudiante
            }
        }

        Estudiante actualizado = estudianteRepository.save(existente);
        return estudianteMapper.toDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarPorId(Long id) {
        if (!estudianteRepository.existsById(id)) {
            throw new EstudianteNotFoundException("No existe estudiante con ID: " + id);
        }
        estudianteRepository.deleteById(id);
    }

    @Override
    @Transactional
    public EstudianteDTO inscribirEstudianteEnCurso(Long estudianteId, Long cursoId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException("Estudiante no encontrado con ID: " + estudianteId));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + cursoId));

        if (estudiante.getCursos().contains(curso)) {
            throw new IllegalStateException("El estudiante ya está inscrito en este curso.");
        }

        estudiante.agregarCurso(curso); // Utiliza el método helper
        Estudiante actualizado = estudianteRepository.save(estudiante);
        return estudianteMapper.toDTO(actualizado);
    }

    @Override
    @Transactional
    public EstudianteDTO desinscribirEstudianteDeCurso(Long estudianteId, Long cursoId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException("Estudiante no encontrado con ID: " + estudianteId));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + cursoId));

        if (!estudiante.getCursos().contains(curso)) {
            throw new IllegalStateException("El estudiante no está inscrito en este curso.");
        }

        estudiante.eliminarCurso(curso); // Utiliza el método helper
        Estudiante actualizado = estudianteRepository.save(estudiante);
        return estudianteMapper.toDTO(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CursoDTO> listarCursosInscritos(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException("Estudiante no encontrado con ID: " + estudianteId));

        return estudiante.getCursos().stream()
                .map(cursoMapper::toDTO)
                .collect(Collectors.toSet());
    }
}