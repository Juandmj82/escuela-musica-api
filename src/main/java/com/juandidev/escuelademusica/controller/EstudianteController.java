package com.juandidev.escuelademusica.controller;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.dto.EstudianteDTO;
import com.juandidev.escuelademusica.service.IEstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstudianteController {

    private final IEstudianteService estudianteService;

    @PostMapping
    public ResponseEntity<EstudianteDTO> crearEstudiante(@Valid @RequestBody EstudianteDTO estudianteDTO) {
        EstudianteDTO guardado = estudianteService.crearEstudiante(estudianteDTO);
        return ResponseEntity.status(201).body(guardado);
    }

    @GetMapping
    public ResponseEntity<List<EstudianteDTO>> listarEstudiantes() {
        // Asumiendo que IEstudianteService.listarEstudiantes ahora devuelve List<EstudianteDTO>
        return ResponseEntity.ok(estudianteService.listarEstudiantes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteDTO> buscarPorId(@PathVariable Long id) {
        // Asumiendo que IEstudianteService.buscarPorId ahora devuelve EstudianteDTO
        return ResponseEntity.ok(estudianteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteDTO> actualizarEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody EstudianteDTO estudianteDTO
    ) {
        return ResponseEntity.ok(estudianteService.actualizarEstudiante(id, estudianteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorId(@PathVariable Long id) {
        estudianteService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{estudianteId}/cursos/{cursoId}")
    public ResponseEntity<EstudianteDTO> inscribirEstudianteEnCurso(
            @PathVariable Long estudianteId,
            @PathVariable Long cursoId
    ) {
        EstudianteDTO estudianteActualizado = estudianteService.inscribirEstudianteEnCurso(estudianteId, cursoId);
        return ResponseEntity.ok(estudianteActualizado);
    }

    @DeleteMapping("/{estudianteId}/cursos/{cursoId}")
    public ResponseEntity<EstudianteDTO> desinscribirEstudianteDeCurso(
            @PathVariable Long estudianteId,
            @PathVariable Long cursoId
    ) {
        EstudianteDTO estudianteActualizado = estudianteService.desinscribirEstudianteDeCurso(estudianteId, cursoId);
        return ResponseEntity.ok(estudianteActualizado);
    }

    @GetMapping("/{estudianteId}/cursos")
    public ResponseEntity<Set<CursoDTO>> listarCursosInscritos(
            @PathVariable Long estudianteId
    ) {
        Set<CursoDTO> cursos = estudianteService.listarCursosInscritos(estudianteId);
        return ResponseEntity.ok(cursos);
    }
}