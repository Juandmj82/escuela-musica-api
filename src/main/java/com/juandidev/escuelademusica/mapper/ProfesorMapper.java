package com.juandidev.escuelademusica.mapper;

import com.juandidev.escuelademusica.dto.ProfesorDTO; // Importa tu ProfesorDTO
import com.juandidev.escuelademusica.model.Profesor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfesorMapper {

    ProfesorDTO toDTO(Profesor entidad);


    Profesor toEntity(ProfesorDTO dto);
}