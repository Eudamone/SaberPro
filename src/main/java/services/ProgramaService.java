package services;

import model.Facultad;
import model.Programa;
import org.springframework.stereotype.Service;
import repository.ProgramaRepository;

import java.util.List;

@Service
public class ProgramaService {
    private final ProgramaRepository programaRepository;

    ProgramaService(ProgramaRepository programaRepository) {
        this.programaRepository = programaRepository;
    }

    public List<Programa> findAll(){
        return programaRepository.findAll();
    }

    public List<Programa> findByCodeFaculty(String codeFaculty){
        return programaRepository.findByFacultad_CodeFaculty(codeFaculty);
    }

    public List<Programa> findProgramsByFaculty(Facultad facultad){
        return  programaRepository.findByFacultad(facultad);
    }
}
