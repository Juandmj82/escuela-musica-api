package com.juandidev.escuelademusica.service;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.dto.ProfesorDTO;
import com.juandidev.escuelademusica.enums.Modalidad;
import com.juandidev.escuelademusica.exception.CursoNotFoundException;
import com.juandidev.escuelademusica.exception.ProfesorNotFoundException;
import com.juandidev.escuelademusica.mapper.CursoMapper;
import com.juandidev.escuelademusica.mapper.ProfesorMapper;
import com.juandidev.escuelademusica.model.Curso;
import com.juandidev.escuelademusica.model.Profesor;
import com.juandidev.escuelademusica.repository.CursoRepository;
import com.juandidev.escuelademusica.repository.ProfesorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importación de la anotación @Transactional de Spring

import java.util.HashSet; // Necesario para la inicialización
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CursoServiceImpl implements ICursoService {

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;
    private final CursoMapper cursoMapper;
    private final ProfesorMapper profesorMapper;

    @Override
    @Transactional // <--- ¡IMPORTANTE! Asegura que la persistencia de la relación ManyToMany ocurra.
    public CursoDTO crearCurso(CursoDTO dto) {
        // Mapea el DTO a la entidad. El mapper ignora el campo 'profesores' por diseño.
        Curso curso = cursoMapper.toEntity(dto);

        // Asegura que la colección de profesores de la entidad no sea null antes de añadir.
        // Aunque la entidad ya la inicializa, es una buena práctica defensiva.
        if (curso.getProfesores() == null) {
            curso.setProfesores(new HashSet<>());
        }

        // Si el DTO contiene IDs de profesores, busca las entidades Profesor y las asigna al Curso.
        if (dto.profesoresIds() != null && !dto.profesoresIds().isEmpty()) {
            // Busca todos los profesores por sus IDs.
            List<Profesor> profesores = profesorRepository.findAllById(dto.profesoresIds());
            // Añade los profesores encontrados a la colección del Curso.
            curso.getProfesores().addAll(profesores);
        }

        // Guarda el Curso. Como estamos dentro de una transacción, JPA persistirá también la relación ManyToMany.
        Curso guardado = cursoRepository.save(curso);

        // Mapea la entidad guardada de vuelta a DTO para el retorno.
        // El CursoMapper ahora está configurado para convertir el Set<Profesor> a Set<Long> (profesoresIds).
        return cursoMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true) // <--- ¡IMPORTANTE! Permite que las colecciones LAZY se carguen.
    public List<CursoDTO> listarCursos() {
        return cursoRepository.findAll() // Encuentra todos los cursos. Dentro de esta transacción,
                .stream()                 // las colecciones LAZY (profesores) pueden ser inicializadas.
                .map(cursoMapper::toDTO)  // Mapea cada entidad Curso a CursoDTO.
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // <--- ¡IMPORTANTE! Permite que las colecciones LAZY se carguen.
    public CursoDTO buscarPorId(Long id) {
        Curso curso = cursoRepository.findById(id) // Busca el curso por ID. Dentro de esta transacción,
                .orElseThrow(()-> new CursoNotFoundException("Curso no encontrado con ID: " + id)); // la colección LAZY puede ser inicializada.
        return cursoMapper.toDTO(curso); // Mapea la entidad encontrada a CursoDTO.
    }

    @Override
    @Transactional // Asegura que la operación de actualización de la relación se guarde.
    public CursoDTO actualizaCurso(Long id, CursoDTO cursoDTO) {
        Curso existente = cursoRepository.findById(id)
                .orElseThrow(()-> new CursoNotFoundException("Curso no encontrado con ID: " + id));

        existente.setNombre(cursoDTO.nombre());
        existente.setDescripcion(cursoDTO.descripcion());
        existente.setModalidad(cursoDTO.modalidad());

        // Limpiar la colección existente de profesores y añadir los nuevos
        // Asegúrate de que existente.getProfesores() se cargue dentro de esta transacción antes del clear().
        existente.getProfesores().clear();
        if (cursoDTO.profesoresIds() != null && !cursoDTO.profesoresIds().isEmpty()) {
            List<Profesor> profesores = profesorRepository.findAllById(cursoDTO.profesoresIds());
            existente.getProfesores().addAll(profesores);
        }

        Curso actualizado = cursoRepository.save(existente);
        return cursoMapper.toDTO(actualizado);
    }

    @Override
    @Transactional // Se recomienda que las operaciones de eliminación sean transaccionales.
    public void eliminarPorId(Long id) {
        if(!cursoRepository.existsById(id)){
            throw new CursoNotFoundException("No existe curso con ID: "+ id);
        }
        cursoRepository.deleteById(id);
    }

    @Override
    @Transactional // Esta operación modifica la relación, debe ser transaccional.
    public CursoDTO agregarProfesorACurso(Long cursoId, Long profesorId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + cursoId));

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ProfesorNotFoundException("Profesor no encontrado con ID: " + profesorId));

        if (curso.getProfesores().contains(profesor)) {
            throw new IllegalStateException("El profesor ya está asignado a este curso");
        }

        curso.agregarProfesor(profesor); // Usa el método helper de la entidad

        Curso actualizado = cursoRepository.save(curso);
        return cursoMapper.toDTO(actualizado);
    }

    @Override
    @Transactional // Esta operación modifica la relación, debe ser transaccional.
    public void eliminarProfesorDeCurso(Long cursoId, Long profesorId) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + cursoId));

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ProfesorNotFoundException("Profesor no encontrado con ID: " + profesorId));

        if (!curso.getProfesores().contains(profesor)) {
            throw new IllegalStateException("El profesor no está asignado a este curso");
        }

        curso.eliminarProfesor(profesor); // Usa el método helper de la entidad

        // Si la relación es unidireccional o la eliminación se propaga, el save puede no ser necesario.
        // Pero si el lado del Curso es el dueño de la relación con CascadeType.ALL o MERGE/PERSIST,
        // JPA maneja el cambio al terminar la transacción. Si no se propaga, puede que necesites un save aquí.
        // Para asegurar, un save después de la modificación es una buena práctica si hay dudas.
        cursoRepository.save(curso); // Asegura que los cambios en la colección se persistan.
    }

    @Override
    @Transactional(readOnly = true) // Carga los profesores para mapear a DTOs.
    public Set<ProfesorDTO> listarProfesoresDelCurso(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNotFoundException("Curso no encontrado con ID: " + id));

        // Obtener y mapear los profesores
        return Optional.ofNullable(curso.getProfesores())
                .orElse(Set.of()) // Si es null (no debería con inicialización), retorna un Set vacío.
                .stream()
                .map(profesorMapper::toDTO)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true) // Carga los cursos.
    public List<CursoDTO> buscarPorModalidad(Modalidad modalidad) {
        return cursoRepository.findByModalidad(modalidad)
                .stream()
                .map(cursoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true) // Carga los cursos.
    public List<CursoDTO> buscarPorNombreContaining(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return cursoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(cursoMapper::toDTO)
                .collect(Collectors.toList());
    }
}