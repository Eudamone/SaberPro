package service;

import model.ExternalGeneralResult;
import model.ExternalSpecificModuleResult;
import model.ExternalSpecificResult;
import model.InternalResult;
import model.Ciudad;
import model.Departamento;
import model.Modulo;

import repository.ExternalGeneralResultRepository;
import repository.ExternalSpecificModuleResultRepository;
import repository.ExternalSpecificResultRepository;
import repository.InternalResultRepository;
import repository.CiudadRepository;
import repository.DepartamentoRepository;
import repository.ModuloRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class FileProcessingService {

    private final ExternalGeneralResultRepository generalRepo;
    private final ExternalSpecificResultRepository specificRepo;
    private final InternalResultRepository internalRepo;
    private final CiudadRepository ciudadRepo;
    private final DepartamentoRepository departamentoRepo;
    private final ModuloRepository moduloRepo;
    private final ExternalSpecificModuleResultRepository moduleResultRepo;


    private static final int BATCH_SIZE_GENERAL = 2000;
    private static final int BATCH_SIZE_SPEC = 500;
    private static final int BATCH_SIZE_INTERNAL = 1000;

    public FileProcessingService(ExternalGeneralResultRepository generalRepo,
                                 ExternalSpecificResultRepository specificRepo,
                                 InternalResultRepository internalRepo,
                                 CiudadRepository ciudadRepo,
                                 DepartamentoRepository departamentoRepo,
                                 ModuloRepository moduloRepo,
                                 ExternalSpecificModuleResultRepository moduleResultRepo) {
        this.generalRepo = generalRepo;
        this.specificRepo = specificRepo;
        this.internalRepo = internalRepo;
        this.ciudadRepo = ciudadRepo;
        this.departamentoRepo = departamentoRepo;
        this.moduloRepo = moduloRepo;
        this.moduleResultRepo = moduleResultRepo;
    }

    private String[] splitLine(String line) {
        return line.split("[;\t,]", -1);
    }

    private Integer parseInteger(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            s = s.replace(",", "");
            return Integer.valueOf(s);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean looksLikeHeader(String headerLine, String[] requiredTokens) {
        if (headerLine == null) return false;
        String low = headerLine.toLowerCase();
        for (String t : requiredTokens) if (!low.contains(t)) return false;
        return true;
    }

    @Transactional
    public List<ExternalGeneralResult> parseAndSaveGeneral(MultipartFile file) throws Exception {
        List<ExternalGeneralResult> savedAll = new ArrayList<>();
        List<ExternalGeneralResult> buffer = new ArrayList<>(BATCH_SIZE_GENERAL);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = br.readLine();
            if (firstLine == null) return savedAll;

            boolean hasHeader = looksLikeHeader(firstLine, new String[]{"periodo","est_consecutivo"});
            String[] headers = hasHeader ? splitLine(firstLine) : null;
            // Requerimos encabezado en archivos generales para poder extraer el campo 'periodo'.
            if (!hasHeader) {
                throw new IllegalArgumentException("El archivo general debe incluir encabezado con las columnas 'periodo' y 'est_consecutivo'. Asegúrese de subir el archivo correcto (separador ';') y que la primera línea contenga esos nombres de columna.");
            }
            Map<String,Integer> idx = new HashMap<>();
            if (headers != null) for (int i=0;i<headers.length;i++) idx.put(headers[i].trim().toLowerCase(), i);

            String line = hasHeader ? br.readLine() : firstLine;
            while (line != null) {
                String[] cols = splitLine(line);
                ExternalGeneralResult r = new ExternalGeneralResult();
                // normalizar periodo para almacenar solo el año (ej. 20121 -> 2012)
                Integer periodoParsed = parsePeriodoAsYear(get(cols, idx, "periodo"));
                if (periodoParsed == null) {
                    throw new IllegalArgumentException("No se pudo determinar el 'periodo' en la línea: '" + line + "'. Asegúrese que la columna 'periodo' tenga valores y que el separador sea ';' o tab.");
                }
                r.setPeriodo(periodoParsed);
                r.setEstConsecutivo(get(cols, idx, "est_consecutivo"));
                r.setEstuDiscapacidad(get(cols, idx, "estu_discapacidad"));
                r.setEstuInstDepartamento(get(cols, idx, "estu_inst_departamento"));
                r.setEstuInstMunicipio(get(cols, idx, "estu_inst_municipio"));
                r.setEstuNucleoPregrado(get(cols, idx, "estu_nucleo_pregrado"));
                r.setEstuPrgmAcademico(get(cols, idx, "estu_prgm_academico"));
                r.setEstuSniesPrgmacademico(get(cols, idx, "estu_snies_prgmacademico"));
                r.setInstCodInstitucion(parseInteger(get(cols, idx, "inst_cod_institucion")));
                r.setInstNombreInstitucion(get(cols, idx, "inst_nombre_institucion"));

                r.setModCompetenCiudadaPnal(parseInteger(get(cols, idx, "mod_competen_ciudada_pnal")));
                r.setModCompetenCiudadaPnbc(parseInteger(get(cols, idx, "mod_competen_ciudada_pnbc")));
                r.setModCompetenCiudadaPunt(parseInteger(get(cols, idx, "mod_competen_ciudada_punt")));
                r.setModComuniEscritaPnal(parseInteger(get(cols, idx, "mod_comuni_escrita_pnal")));
                r.setModComuniEscritaPnbc(parseInteger(get(cols, idx, "mod_comuni_escrita_pnbc")));
                r.setModComuniEscritaPunt(parseInteger(get(cols, idx, "mod_comuni_escrita_punt")));
                r.setModInglesPnal(parseInteger(get(cols, idx, "mod_ingles_pnal")));
                r.setModInglesPnbc(parseInteger(get(cols, idx, "mod_ingles_pnbc")));
                r.setModInglesPunt(parseInteger(get(cols, idx, "mod_ingles_punt")));
                r.setModLecturaCriticaPnal(parseInteger(get(cols, idx, "mod_lectura_critica_pnal")));
                r.setModLecturaCriticaPnbc(parseInteger(get(cols, idx, "mod_lectura_critica_pnbc")));
                r.setModLecturaCriticaPunt(parseInteger(get(cols, idx, "mod_lectura_critica_punt")));
                r.setModRazonaCuantitativoPnal(parseInteger(get(cols, idx, "mod_razona_cuantitativo_pnal")));
                r.setModRazonaCuantitativoPnbc(parseInteger(get(cols, idx, "mod_razona_cuantitativo_pnbc")));
                r.setModRazonaCuantitatPunt(parseInteger(get(cols, idx, "mod_razona_cuantitat_punt")));

                r.setPercentilGlobal(parseInteger(get(cols, idx, "percentil_global")));
                r.setPercentilNbc(parseInteger(get(cols, idx, "percentil_nbc")));
                r.setPuntGlobal(parseInteger(get(cols, idx, "punt_global")));

                r.setCreatedAt(OffsetDateTime.now());
                buffer.add(r);

                if (buffer.size() >= BATCH_SIZE_GENERAL) {
                    List<ExternalGeneralResult> saved = generalRepo.saveAll(new ArrayList<>(buffer));
                    savedAll.addAll(saved);
                    buffer.clear();
                }
                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                List<ExternalGeneralResult> saved = generalRepo.saveAll(buffer);
                savedAll.addAll(saved);
                buffer.clear();
            }
        }
        return savedAll;
    }

    @Transactional
    public List<ExternalSpecificResult> parseAndSaveSpecifics(MultipartFile file, Integer periodo) throws Exception {
        List<ExternalSpecificResult> savedAll = new ArrayList<>();
        List<ExternalSpecificResult> buffer = new ArrayList<>(BATCH_SIZE_SPEC);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = br.readLine();
            if (firstLine == null) return savedAll;

            boolean hasHeader = looksLikeHeader(firstLine, new String[]{"estu_consecutivo","result_nombreprueba"});
            String[] headers = hasHeader ? splitLine(firstLine) : null;
            Map<String,Integer> idx = new HashMap<>();
            if (headers != null) for (int i=0;i<headers.length;i++) idx.put(headers[i].trim().toLowerCase(), i);

            String line = hasHeader ? br.readLine() : firstLine;
            while (line != null) {
                String[] cols = splitLine(line);

                ExternalSpecificResult r = new ExternalSpecificResult();
                // normalizamos el periodo recibido (si viene 20121 -> 2012)
                r.setPeriodo(normalizePeriodo(periodo));
                String estConsec = get(cols, idx, "estu_consecutivo");
                r.setEstuConsecutivo(estConsec);
                String prueba = get(cols, idx, "result_nombreprueba");
                r.setResultNombrePrueba(prueba);
                r.setResultPuntaje(parseInteger(get(cols, idx, "result_puntaje")));
                r.setPercentilNacional(parseInteger(get(cols, idx, "percentil_nacional")));
                r.setPercentilNbc(parseInteger(get(cols, idx, "percentil_nbc")));

                Integer moduloId = resolveOrCreateModuloId(prueba);
                r.setModuloId(moduloId);

                if (r.getPeriodo() != null && estConsec != null) {
                    Optional<ExternalGeneralResult> og = generalRepo.findFirstByPeriodoAndEstConsecutivo(r.getPeriodo(), estConsec);
                    og.ifPresent(g -> r.setExternaId(g.getId()));
                }

                buffer.add(r);

                if (buffer.size() >= BATCH_SIZE_SPEC) {
                    List<ExternalSpecificResult> saved = specificRepo.saveAll(new ArrayList<>(buffer));
                    savedAll.addAll(saved);
                    buffer.clear();
                }
                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                List<ExternalSpecificResult> saved = specificRepo.saveAll(buffer);
                savedAll.addAll(saved);
                buffer.clear();
            }
        }
        return savedAll;
    }

    @Transactional
    public List<InternalResult> parseAndSaveInternal(MultipartFile file) throws Exception {
        List<InternalResult> savedAll = new ArrayList<>();
        List<InternalResult> buffer = new ArrayList<>(BATCH_SIZE_INTERNAL);
        List<TempModuleResult> moduleTempBuffer = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = br.readLine();
            if (firstLine == null) return savedAll;

            boolean hasHeader = looksLikeHeader(firstLine, new String[]{"tipo de documento","documento"});
            String[] headers = hasHeader ? splitLine(firstLine) : null;
            Map<String,Integer> idx = new HashMap<>();
            if (headers != null) for (int i=0;i<headers.length;i++) idx.put(headers[i].trim().toLowerCase(), i);

            String line = hasHeader ? br.readLine() : firstLine;
            while (line != null) {
                String[] cols = splitLine(line);

                InternalResult r = new InternalResult();
                r.setTipoDocumento(get(cols, idx, "tipo de documento"));
                r.setDocumento(get(cols, idx, "documento"));
                r.setNombre(get(cols, idx, "nombre"));
                String numeroRegistro = get(cols, idx, "número de registro");
                r.setNumeroRegistro(numeroRegistro);
                r.setTipoEvaluado(get(cols, idx, "tipo de evaluado"));
                r.setSniesPrograma(get(cols, idx, "snies programa académico"));
                r.setPrograma(get(cols, idx, "programa"));

                String rawCiudad = get(cols, idx, "ciudad");
                String rawDepartamento = get(cols, idx, "departamento");
                Integer ciudadId = resolveCiudadId(rawCiudad);
                Integer departamentoId = resolveDepartamentoId(rawDepartamento);
                r.setCiudadId(ciudadId);
                r.setDepartamentoId(departamentoId);

                r.setGrupoReferencia(get(cols, idx, "grupo de referencia"));
                r.setPuntajeGlobal(parseInteger(get(cols, idx, "puntaje global")));
                r.setPercentilNacionalGlobal(parseInteger(get(cols, idx, "percentil nacional global")));
                r.setPercentilGrupoReferencia(parseInteger(get(cols, idx, "percentil grupo de referencia")));
                r.setCreatedAt(OffsetDateTime.now());

                int bufferIndex = buffer.size();
                buffer.add(r);

                String moduloNombre = get(cols, idx, "módulo");
                if (moduloNombre == null) moduloNombre = get(cols, idx, "modulo");
                Integer puntajeModulo = parseInteger(get(cols, idx, "puntaje módulo"));
                Integer percentilNacModulo = parseInteger(get(cols, idx, "percentil nacional modulo"));
                Integer percentilGrupoModulo = parseInteger(get(cols, idx, "percentil grupo de referencia modulo"));

                if (moduloNombre != null || puntajeModulo != null || percentilNacModulo != null || percentilGrupoModulo != null) {
                    TempModuleResult tmp = new TempModuleResult();
                    tmp.internalIndex = bufferIndex;
                    tmp.moduloNombre = moduloNombre;
                    tmp.puntaje = puntajeModulo;
                    tmp.percentilNacional = percentilNacModulo;
                    tmp.percentilGrupoReferencia = percentilGrupoModulo;
                    moduleTempBuffer.add(tmp);
                }

                if (buffer.size() >= BATCH_SIZE_INTERNAL) {
                    List<InternalResult> saved = internalRepo.saveAll(new ArrayList<>(buffer));
                    // link module results by preserving order
                    linkAndSaveModuleResults(saved, moduleTempBuffer);
                    savedAll.addAll(saved);
                    buffer.clear();
                    moduleTempBuffer.clear();
                }

                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                List<InternalResult> saved = internalRepo.saveAll(buffer);
                linkAndSaveModuleResults(saved, moduleTempBuffer);
                savedAll.addAll(saved);
                buffer.clear();
                moduleTempBuffer.clear();
            }
        }
        return savedAll;
    }

    private String get(String[] cols, Map<String,Integer> idx, String key) {
        Integer i = idx.get(key.toLowerCase());
        if (i == null || i < 0 || i >= cols.length) {
            for (Map.Entry<String,Integer> e : idx.entrySet()) {
                String k = e.getKey().replace(" ", "").replace("í","i").replace("ó","o");
                String kk = key.toLowerCase().replace(" ", "").replace("í","i").replace("ó","o");
                if (k.equals(kk)) return cols[e.getValue()].trim().isEmpty() ? null : cols[e.getValue()].trim();
            }
            return null;
        }
        String v = cols[i].trim();
        return v.isEmpty() ? null : v;
    }

    private Integer resolveCiudadId(String raw) {
        if (raw == null) return null;
        Integer asInt = parseInteger(raw);
        if (asInt != null) return asInt;
        String name = raw.trim();
        Optional<Ciudad> oc = ciudadRepo.findFirstByNombreIgnoreCase(name);
        if (oc.isPresent()) return oc.get().getIdCiudad();
        String n2 = name.replace("í","i").replace("ó","o").trim();
        oc = ciudadRepo.findFirstByNombreIgnoreCase(n2);
        if (oc.isPresent()) return oc.get().getIdCiudad();
        Ciudad nueva = new Ciudad();
        nueva.setNombre(name);
        Ciudad saved = ciudadRepo.save(nueva);
        return saved.getIdCiudad();
    }

    private Integer resolveDepartamentoId(String raw) {
        if (raw == null) return null;
        Integer asInt = parseInteger(raw);
        if (asInt != null) return asInt;
        String name = raw.trim();
        Optional<Departamento> od = departamentoRepo.findFirstByNombreIgnoreCase(name);
        if (od.isPresent()) return od.get().getIdDepartamento().intValue();
        String n2 = name.replace("í","i").replace("ó","o").trim();
        od = departamentoRepo.findFirstByNombreIgnoreCase(n2);
        if (od.isPresent()) return od.get().getIdDepartamento().intValue();
        Departamento nuevo = new Departamento();
        nuevo.setNombre(name);
        Departamento saved = departamentoRepo.save(nuevo);
        return Integer.valueOf(saved.getIdDepartamento());
    }

    private Integer resolveOrCreateModuloId(String nombrePrueba) {
        if (nombrePrueba == null) return null;
        String normalized = nombrePrueba.trim();
        Optional<Modulo> om = moduloRepo.findFirstByNombreIgnoreCase(normalized);
        if (om.isPresent()) return om.get().getIdModulo();
        Modulo nuevoModulo = new Modulo();
        nuevoModulo.setNombre(normalized);
        Modulo savedModulo = moduloRepo.save(nuevoModulo);
        return savedModulo.getIdModulo();
    }

    // Normaliza el periodo para almacenar solo el año.
    // Ejemplos: 20121 -> 2012 (divide por 10 si el valor tiene más de 4 dígitos),
    // 2020 -> 2020 (ya solo año)
    private Integer normalizePeriodo(Integer periodo) {
        if (periodo == null) return null;
        if (Math.abs(periodo) > 9999) {
            return periodo / 10;
        }
        return periodo;
    }

    private Integer parsePeriodoAsYear(String raw) {
        Integer p = parseInteger(raw);
        return normalizePeriodo(p);
    }

    private void linkAndSaveModuleResults(List<InternalResult> savedInternals, List<TempModuleResult> tempModules) {
        List<ExternalSpecificModuleResult> toSave = new ArrayList<>();
        for (TempModuleResult t : tempModules) {
            if (t.internalIndex >= 0 && t.internalIndex < savedInternals.size()) {
                InternalResult related = savedInternals.get(t.internalIndex);
                if (related != null) {
                    ExternalSpecificModuleResult rm = new ExternalSpecificModuleResult();
                    rm.setInternoId(related.getId());
                    Integer modId = resolveOrCreateModuloId(t.moduloNombre);
                    rm.setModuloId(modId);
                    rm.setPuntaje(t.puntaje);
                    rm.setPercentilNacional(t.percentilNacional);
                    rm.setPercentilGrupoReferencia(t.percentilGrupoReferencia);
                    toSave.add(rm);
                }
            }
        }
        if (!toSave.isEmpty()) moduleResultRepo.saveAll(toSave);
    }

    private static class TempModuleResult {
        int internalIndex = -1;
        String moduloNombre;
        Integer puntaje;
        Integer percentilNacional;
        Integer percentilGrupoReferencia;
    }

    public boolean existsGeneralPeriod(Integer periodo) {
        // normalizamos periodo a año antes de consultar
        return generalRepo.existsByPeriodo(normalizePeriodo(periodo));
    }

}
