package com.juandidev.escuelademusica.controller;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.dto.ProfesorDTO;
import com.juandidev.escuelademusica.enums.Modalidad;
import com.juandidev.escuelademusica.service.ICursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CursoController {

    private final ICursoService cursoService;

    @PostMapping
    public ResponseEntity<CursoDTO> crearCurso(@Valid @RequestBody CursoDTO cursoDTO) {
        CursoDTO guardado = cursoService.crearCurso(cursoDTO);
        return ResponseEntity.status(201).body(guardado);

    }

    @GetMapping
    public ResponseEntity<List<CursoDTO>> listarCursos(){
        return ResponseEntity.ok(cursoService.listarCursos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(cursoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoDTO> actualizarCurso(@PathVariable Long id, @Valid @RequestBody CursoDTO cursoDTO){
        return  ResponseEntity.ok(cursoService.actualizaCurso(id, cursoDTO));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id){
         cursoService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }

    //Endpoints adicionales para la relación profesor curso y viceversa
    @PostMapping("/{cursoId}/profesores/{profesorId}")
    public ResponseEntity<CursoDTO> agregarProfesorACurso(@PathVariable Long cursoId, @PathVariable Long profesorId ){
        return ResponseEntity.ok(cursoService.agregarProfesorACurso(cursoId, profesorId));
    }

    @DeleteMapping("/{cursoId}/profesores/{profesorId}")
    public ResponseEntity<Void> eliminarProfesorDeCurso(
            @PathVariable Long cursoId,
            @PathVariable Long profesorId) {
        cursoService.eliminarProfesorDeCurso(cursoId, profesorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/profesores")
    public ResponseEntity<Set<ProfesorDTO>> listarProfesoresDelCurso(@PathVariable Long id){
        return ResponseEntity.ok(cursoService.listarProfesoresDelCurso(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CursoDTO>> buscarPorNombre(@RequestParam (required = false) String nombre, @RequestParam(required = false)Modalidad modalidad){
        if(nombre != null){
            return ResponseEntity.ok(cursoService.buscarPorNombreContaining(nombre));
        }else if(modalidad != null){
            return ResponseEntity.ok(cursoService.buscarPorModalidad(modalidad));
        }else{
            return ResponseEntity.badRequest().build();
        }

    }






}
