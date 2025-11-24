package services;

import dto.*;
import model.Facultad;
import model.Modulo;
import model.Programa;
import org.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final ModuloRepository moduloRepository;
    private final ReportExportService reportExportService;


    CatalogService(
            ProgramaRepository programaRepository,
            FacultadRepository facultadRepository,
            UsuarioRepository usuarioRepository,
            InternalResultRepository internalResultRepository,
            ExternalGeneralResultRepository externalGeneralResultRepository,
            InternaModuleResultRepository internalModuleResultRepository,
            ModuloRepository moduloRepository,
            ReportExportService reportExportService) {
        this.programaRepository = programaRepository;
        this.facultadRepository = facultadRepository;
        this.usuarioRepository = usuarioRepository;
        this.internalResultRepository = internalResultRepository;
        this.externalGeneralResultRepository = externalGeneralResultRepository;
        this.internalModuleResultRepository = internalModuleResultRepository;
        this.moduloRepository = moduloRepository;
        this.reportExportService = reportExportService;
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

    public Page<InternResultInfo> findInternResults(int page,int size, InternResultFilter filter){
        PageRequest pageable = PageRequest.of(page, size);
        return internalResultRepository.findResults(pageable, filter);
    }

    public Integer sizeInternResults(){
        return internalResultRepository.sizeInternResultsAll();
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

    public List<PromedioAnioDTO> getPromediosAnio(){
        return internalResultRepository.getPromedyForAnio();
    }

    public List<PromedioProgram> getPromedioProgramasFacultad(String codeFaculty){
        return internalResultRepository.getPromedyProgramsFaculty(codeFaculty);
    }

    public String getCodeFaculty(Long idDean){
        return facultadRepository.getCodeByDean(idDean);
    }

    // Por programa general todos los periodos
    public List<ModuloPromedio> getModuloPromedio(String programa){
        return moduloRepository.getPromedyModuleByProgram(programa);
    }

    // Por programa en un periodo específico
    public List<ModuloPromedio> getModuloPromedioByAnio(String programa,Integer periodo){
        return moduloRepository.getPromedyModuleByProgramForAnio(programa,periodo);
    }

    // General (toda la universidad) por un periodo específico
    public List<ModuloPromedio> getModuloPromedioGeneralByAnio(Integer periodo){
        return moduloRepository.getPromedyModuloGeneralForAnio(periodo);
    }

    public Integer getPromedyGeneralFacultadDean(Long idDean){
        String codeFaculty = getCodeFaculty(idDean);
        Double promedio = internalResultRepository.getPromedyGeneralByFacultad(codeFaculty);
        return (Integer) (int) Math.round(promedio);
    }

    public Integer getPromedyExtern(){
        Double promedio = externalGeneralResultRepository.getPromedyGeneral();
        return (Integer) (int)  Math.round(promedio);
    }

    public Integer getPercentilGeneralFacultadDean(Long idDean){
        String codeFaculty = getCodeFaculty(idDean);
        Double promedio = internalResultRepository.getPercentilGeneralFacultadDean(codeFaculty);
        return (Integer) (int)  Math.round(promedio);
    }

    public Integer getPercentilGeneral(){
        Double promedio = externalGeneralResultRepository.getPercentilGeneral();
        return (Integer) (int)  Math.round(promedio);
    }

    public long countInternResults(InternResultFilter filter){
        return internalResultRepository.countResults(filter);
    }

    public List<Integer> getSemesters(){
        return internalResultRepository.getSemesters();
    }

    public InternResultReport generateInternReport(InternResultFilter filter) {
        return internalResultRepository.generateReport(filter == null ? new InternResultFilter() : filter);
    }

    //--------------------- Métodos para resultados estudiantes ------------------------

    public Integer getPeriodoResult(String numIdentification){
        return internalResultRepository.getPeriodoByStudent(numIdentification);
    }

    public String getProgramStudent(String numIdentification){
        return internalResultRepository.getProgramaStudent(numIdentification);
    }

    // Resultados a nivel de universidad
    public Integer getSizeInternalResultAnio(String numIdentification){
        Integer periodo =  getPeriodoResult(numIdentification);
        return internalResultRepository.sizeInternResultsByAnio(periodo);
    }

    // Resultados a nivel de programa
    public Integer getSizeInternalResultAnioProgram(String numIdentification){
        String programa =  getProgramStudent(numIdentification);
        Integer periodo =  getPeriodoResult(numIdentification);
        return internalResultRepository.sizeInternalResultsByPrograma(periodo,programa);
    }

    public Integer getPuestoUniversidad(String numIdentification){
        Integer periodo = getPeriodoResult(numIdentification);
        return internalResultRepository.getPuestoUniversidadByAnio(periodo, numIdentification);
    }

    public Integer getPuestoPrograma(String numIdentification){
        Integer periodo = getPeriodoResult(numIdentification);
        String programa =  getProgramStudent(numIdentification);
        return internalResultRepository.getPuestoProgramaByAnio(periodo,numIdentification,programa);
    }

    public MejorModulo getMejorModulo(String numIdentification){
        return internalResultRepository.getMejorModuloStudent(numIdentification);
    }

    public Integer getPercentilNacionalStudent(String numIdentification){
        return internalResultRepository.getPercentilNacionalStudent(numIdentification);
    }

    public Integer getPuntajeGlobalStudent(String numIdentification){
        return internalResultRepository.getPuntajeGlobalStudent(numIdentification);
    }

    public List<ModuloPromedio> getPromedioModulosStudent(String numIdentification){
        return moduloRepository.getPromedyModuleByStudent(numIdentification);
    }

    // Se obtiene el listado de los mejores promedios de 4 universidades
    public List<UniversidadPromedio> getMejoresUniversidadPorPeriodo(String numIdentification){
        Integer periodo = getPeriodoResult(numIdentification);
        return externalGeneralResultRepository.getMejoresPromedioUniversidades(periodo);
    }


    public JSONObject buildReportPayload(InternResultFilter filter) {
        InternResultReport report = generateInternReport(filter);
        return report == null ? new JSONObject() : reportExportService.buildPayload(report);
    }

    public String buildReportPrompt(InternResultFilter filter) {
        InternResultReport report = generateInternReport(filter);
        return report == null ? "" : reportExportService.buildPrompt(report);
    }
}
