package com.juandidev.escuelademusica.mapper;

import com.juandidev.escuelademusica.dto.EstudianteDTO;
import com.juandidev.escuelademusica.model.Curso;
import com.juandidev.escuelademusica.model.Estudiante;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EstudianteMapper {

    // Convierte un DTO en una entidad Estudiante
    // IMPORTANTE: Ignoramos el campo 'cursos' de la entidad al mapear desde el DTO.
    // El servicio es responsable de buscar y asignar las entidades Curso reales
    // basándose en los cursosIds del DTO.
    @Mapping(target = "cursos", ignore = true)
    Estudiante toEntity(EstudianteDTO dto);

    // Convierte una entidad Estudiante en un DTO
    // Mapeamos la colección 'cursos' (Set<Curso>) de la entidad al campo 'cursosIds' (Set<Long>) del DTO.
    @Mapping(target = "cursosIds", source = "cursos", qualifiedByName = "cursosToIds")
    EstudianteDTO toDTO(Estudiante estudiante);

    // Método personalizado para mapear Set<Curso> a Set<Long>
    @Named("cursosToIds")
    default Set<Long> cursosToIds(Set<Curso> cursos) {
        return cursos != null ?
                cursos.stream()
                        .map(Curso::getId)
                        .collect(Collectors.toSet()) :
                Collections.emptySet();
    }
}