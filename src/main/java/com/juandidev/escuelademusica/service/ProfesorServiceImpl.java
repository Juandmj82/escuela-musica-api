package com.juandidev.escuelademusica.service;

import com.juandidev.escuelademusica.dto.ProfesorDTO;
import com.juandidev.escuelademusica.exception.ProfesorNotFoundException;
import com.juandidev.escuelademusica.mapper.ProfesorMapper;
import com.juandidev.escuelademusica.model.Profesor;
import com.juandidev.escuelademusica.repository.ProfesorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProfesorServiceImpl implements IProfesorService {

    private final ProfesorRepository profesorRepository;
    private final ProfesorMapper profesorMapper;


    @Override
    public ProfesorDTO crearProfesor(ProfesorDTO dto) {
        Profesor profesor = profesorMapper.toEntity(dto);
        Profesor guardado = profesorRepository.save(profesor);
        return profesorMapper.toDTO(guardado);
    }

    @Override
    public List<ProfesorDTO> listarProfesores() {
        return profesorRepository.findAll()
                .stream()
                .map(profesorMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProfesorDTO buscarPorId(Long id) {
        Profesor profesor = profesorRepository.findById(id)
                .orElseThrow(()-> new ProfesorNotFoundException("Profesor no encontrado con ID: "+ id));
        return profesorMapper.toDTO(profesor);
    }

    @Override
    public ProfesorDTO actualizarProfesor(Long id, ProfesorDTO profesorDTO) {
        Profesor existente = profesorRepository.findById(id)
                .orElseThrow(() -> new ProfesorNotFoundException("Profesor no encontrado con ID: " + id));

        existente.setNombre(profesorDTO.nombre());
        existente.setApellidos(profesorDTO.apellidos());
        existente.setEmail(profesorDTO.email());
        existente.setTelefono(profesorDTO.telefono());
        existente.setInstrumento(profesorDTO.instrumento());
        existente.setModalidad(profesorDTO.modalidad());

        Profesor actualizado = profesorRepository.save(existente);


        return  profesorMapper.toDTO(actualizado);
    }

    @Override
    public void eliminarPorId(Long id) {

        if(!profesorRepository.existsById(id)){
            throw new ProfesorNotFoundException("No existe profesor con ID: " + id);

        }
        profesorRepository.deleteById(id);

    }


}
