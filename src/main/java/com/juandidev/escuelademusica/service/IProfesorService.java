package com.juandidev.escuelademusica.service;

import com.juandidev.escuelademusica.dto.ProfesorDTO;


import java.util.List;

public interface IProfesorService {

    ProfesorDTO crearProfesor(ProfesorDTO dto);

    List<ProfesorDTO> listarProfesores();

    ProfesorDTO buscarPorId(Long id);

    ProfesorDTO actualizarProfesor(Long id, ProfesorDTO profesorDTO);

    void eliminarPorId(Long id);
}
