package services;


import dto.UsuarioInfoDTO;
import model.Facultad;
import model.Programa;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import repository.FacultadRepository;
import repository.ProgramaRepository;
import repository.UsuarioRepository;

import java.util.List;

@Service
public class CatalogService {

    private ProgramaRepository  programaRepository;
    private FacultadRepository  facultadRepository;
    private UsuarioRepository usuarioRepository;

    CatalogService(ProgramaRepository programaRepository, FacultadRepository facultadRepository,UsuarioRepository usuarioRepository) {
        this.programaRepository = programaRepository;
        this.facultadRepository = facultadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Cacheable("facultades")
    public List<Facultad>  findAllFacultades(){
        return facultadRepository.findAllFacultades();
    }

    @CacheEvict(value = "facultades", allEntries = true)
    public void clearFacultadesCache() {
        System.out.println("Limpiando caché de facultades");
    }

    @Cacheable(value = "programas",key = "#facultad.codeFaculty")
    public List<Programa> findAllProgramas(Facultad facultad){
        return programaRepository.findByFacultad(facultad);
    }

    @CacheEvict(value = "programas",key = "#facultad.codeFaculty")
    public void clearProgramasCache(Facultad facultad){
        System.out.println("Limpiando programas");
    }

    @Cacheable("usuariosTabla")
    public List<UsuarioInfoDTO> findAllUsersTable(){
        return usuarioRepository.findUsuarioInfo();
    }

    @CacheEvict(value = "usuariosTabla",allEntries = true)
    public void clearCacheUsuarios(){
        System.out.println("Limpiando usuarios");
    }

    @CacheEvict(value = {"facultades,programas"},allEntries = true)
    public void clearAllCache(){
        System.out.println("Limpiando cache");
    }

}
