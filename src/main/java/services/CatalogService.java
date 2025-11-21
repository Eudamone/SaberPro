package services;

import dto.InternResultFilter;
import dto.InternResultInfo;
import dto.UsuarioInfoDTO;
import model.Facultad;
import model.Programa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import repository.*;

import utils.Normalized;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CatalogService {

    private final ProgramaRepository  programaRepository;
    private final FacultadRepository  facultadRepository;
    private final UsuarioRepository usuarioRepository;
    private final InternalResultRepository internalResultRepository;
    private final ExternalGeneralResultRepository  externalGeneralResultRepository;
    private final InternaModuleResultRepository internalModuleResultRepository;


    CatalogService(
            ProgramaRepository programaRepository,
            FacultadRepository facultadRepository,
            UsuarioRepository usuarioRepository,
            InternalResultRepository internalResultRepository,
            ExternalGeneralResultRepository externalGeneralResultRepository,
            InternaModuleResultRepository internalModuleResultRepository) {
        this.programaRepository = programaRepository;
        this.facultadRepository = facultadRepository;
        this.usuarioRepository = usuarioRepository;
        this.internalResultRepository = internalResultRepository;
        this.externalGeneralResultRepository = externalGeneralResultRepository;
        this.internalModuleResultRepository = internalModuleResultRepository;
    }

    public List<Facultad>  findAllFacultades(){
        return facultadRepository.findAllFacultades();
    }

    public void clearFacultadesCache() {
        System.out.println("Limpiando caché de facultades");
    }

    public List<Programa> findAllProgramas(Facultad facultad){
        return programaRepository.findByFacultad(facultad);
    }

    public void clearProgramasCache(Facultad facultad){
        System.out.println("Limpiando programas");
    }

    public List<UsuarioInfoDTO> findAllUsersTable(){
        return usuarioRepository.findUsuarioInfo();
    }

    public void clearCacheUsuarios(){
        System.out.println("Limpiando usuarios");
    }

    public void clearAllCache(){
        System.out.println("Limpiando cache");
    }

    public Page<InternResultInfo> findInternResults(int page,int size, InternResultFilter filter){
        PageRequest pageable = PageRequest.of(page,size, Sort.by("id").ascending());
        return internalResultRepository.findResults(pageable, filter);
    }



    public long countInternResults(InternResultFilter filter){
        return internalResultRepository.countResults(filter);
    }

    public List<String> getProgramsDean(Long id){
        return programaRepository.findByCodeDean(id);
    }

    public List<Integer> getPeriodsResult(){
        return internalResultRepository.getPeriods();
    }

    public Set<String> getNBCDean(Long id){
        List<String> programas = getProgramsDean(id).stream()
                .map(Normalized::limpiarYMayusculas)
                .toList();
        Set<String> NBC = new HashSet<>();
        for(String programa : programas){
            NBC.addAll(externalGeneralResultRepository.getNBCs(programa));
        }
        return NBC;
    }

    public List<String> getAreas(){
        return internalModuleResultRepository.getAreas();
    }
}
