package com.juandidev.escuelademusica.mapper;

import com.juandidev.escuelademusica.dto.CursoDTO;
import com.juandidev.escuelademusica.model.Curso;
import com.juandidev.escuelademusica.model.Profesor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections; // Importación necesaria para Collections.emptySet()
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = { ProfesorMapper.class })
public interface CursoMapper {

    // Convierte un DTO en una entidad Curso
    // IMPORTANTE: Ignoramos el campo 'profesores' de la entidad al mapear desde el DTO.
    // El servicio será responsable de buscar y asignar las entidades Profesor reales
    // basándose en los profesoresIds del DTO.
    @Mapping(target = "profesores", ignore = true)
    Curso toEntity(CursoDTO dto);

    // Convierte una entidad Curso en un DTO
    // Mapeamos la colección 'profesores' (Set<Profesor>) de la entidad al campo 'profesoresIds' (Set<Long>) del DTO.
    @Mapping(target = "profesoresIds", source = "profesores", qualifiedByName = "profesoresAIds")
    CursoDTO toDTO(Curso entidad);

    // Método personalizado para mapear Set<Profesor> a Set<Long>
    @Named("profesoresAIds")
    default Set<Long> profesoresAIds(Set<Profesor> profesores) {
        return profesores != null ?
                profesores.stream()
                        .map(Profesor::getId) // Mapea cada objeto Profesor a su ID
                        .collect(Collectors.toSet()) : // Recolecta los IDs en un Set
                Collections.emptySet(); // Devuelve un Set vacío si profesores es null
    }


}