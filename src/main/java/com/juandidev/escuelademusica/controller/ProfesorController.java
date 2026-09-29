package com.juandidev.escuelademusica.controller;

import com.juandidev.escuelademusica.dto.ProfesorDTO;
import com.juandidev.escuelademusica.service.IProfesorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profesores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfesorController {

    private final IProfesorService profesorService;

    @PostMapping
    public ResponseEntity<ProfesorDTO> crearProfesor(@Valid @RequestBody ProfesorDTO profesorDTO) {
        ProfesorDTO guardado = profesorService.crearProfesor(profesorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping
    public ResponseEntity<List<ProfesorDTO>> listarProfesores() {
        return ResponseEntity.ok(profesorService.listarProfesores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesorDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(profesorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesorDTO> actualizarProfesor(
            @PathVariable Long id,
            @Valid @RequestBody ProfesorDTO profesorDTO) {
        return ResponseEntity.ok(profesorService.actualizarProfesor(id, profesorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorId(@PathVariable Long id) {
        profesorService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }
}