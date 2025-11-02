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
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer; // añadido

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

    // Split que usa sólo ';' o ',' (no soportamos espacio o tab)
    private String[] splitLine(String line) {
        if (line == null) return new String[0];
        String delimiter = ";";
        if (line.contains(";")) delimiter = ";";
        else if (line.contains(",")) delimiter = ",";
        return line.split(java.util.regex.Pattern.quote(delimiter), -1);
    }

    // Split usando el delimitador detectado (por ejemplo ';' o ',')
    private String[] splitLine(String line, String delimiter) {
        if (line == null) return new String[0];
        // usar Pattern.quote para evitar que el delimiter sea tratado como regex
        return line.split(java.util.regex.Pattern.quote(delimiter), -1);
    }

    private Integer parseInteger(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        // eliminar espacios y separadores de miles (coma como separador de miles)
        s = s.replace("\u00A0", "").replace(" ", "");
        try {
            // si es entero puro
            if (s.matches("^-?\\d+$")) {
                return Integer.valueOf(s);
            }
            // permitir decimales con punto o coma: 12.1 o 12,1
            String asDot = s.replace(',', '.');
            double d = Double.parseDouble(asDot);
            // redondear al entero más cercano
            return (int) Math.round(d);
        } catch (Exception e) {
            return null;
        }
    }

    // Nuevo: parsear BigDecimal manteniendo precisión y retornando null si no válido
    private BigDecimal parseBigDecimal(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        s = s.replace("\u00A0", "").replace(" ", "");
        // Reemplazar comas decimales por punto
        String normalized = s.replace(',', '.');
        try {
            return new BigDecimal(normalized);
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

            // Depuración: mostrar header leído y detectar delimitador (coherente con TxtReaderService)
            // Normalizar primera línea: quitar BOM y espacios, y crear versión en minúsculas para reconocimiento
            String cleanedFirstLine = firstLine.replace("\uFEFF", "").trim();
            String detectedDelimiter = ";";
            if (cleanedFirstLine.contains(";")) detectedDelimiter = ";";
            else if (cleanedFirstLine.contains(",")) detectedDelimiter = ",";
            System.out.println("[FileProcessingService] Header detectado: '" + cleanedFirstLine + "'");
            System.out.println("[FileProcessingService] Delimitador detectado: '" + detectedDelimiter + "'");

            // Construir mapa de encabezados a partir de la primera línea usando el delimitador detectado
            String[] headers = splitLine(cleanedFirstLine, detectedDelimiter);
            Map<String, Integer> headerMap = new HashMap<>();
            Map<String, Integer> normalizedMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String raw = headers[i] == null ? "" : headers[i].trim();
                String lower = raw.toLowerCase().replace("\uFEFF", "");
                headerMap.put(lower, i);
                String norm = lower.replace(" ", "").replace("-", "").replace("_", "").replace("í", "i").replace("ó", "o");
                normalizedMap.put(norm, i);
            }

            // Intentar mapear columnas mínimas con tolerancia (variantes de nombres)
            Integer idxPeriodo = null;
            Integer idxEstu = null;
            if (normalizedMap.containsKey("periodo")) idxPeriodo = normalizedMap.get("periodo");
            else {
                for (String k : normalizedMap.keySet())
                    if (k.contains("periodo")) {
                        idxPeriodo = normalizedMap.get(k);
                        break;
                    }
            }
            if (normalizedMap.containsKey("estuconsecutivo")) idxEstu = normalizedMap.get("estuconsecutivo");
            else if (normalizedMap.containsKey("estconsecutivo")) idxEstu = normalizedMap.get("estconsecutivo");
            else {
                // buscar cualquier encabezado que contenga 'consec' o 'consecut'
                for (String k : normalizedMap.keySet())
                    if (k.contains("consec") || k.contains("consecut")) {
                        idxEstu = normalizedMap.get(k);
                        break;
                    }
            }

            if (idxPeriodo == null || idxEstu == null) {
                throw new IllegalArgumentException("El archivo general debe incluir encabezado con las columnas 'periodo' y 'est_consecutivo' (o nombres equivalentes). Encabezados detectados: " + headerMap.keySet());
            }

            // Construir índice final con claves canónicas
            Map<String, Integer> idx = new HashMap<>();
            idx.put("periodo", idxPeriodo);
            idx.put("est_consecutivo", idxEstu);
            // añadir el resto de headers para acceso flexible: incluir tanto la clave original (lower) como su versión normalizada
            for (Map.Entry<String, Integer> e : headerMap.entrySet()) {
                String rawKey = e.getKey();
                Integer pos = e.getValue();
                idx.putIfAbsent(rawKey, pos);
                // also add normalized form
                String normKey = rawKey.replace(" ", "").replace("-", "").replace("_", "").replace("í", "i").replace("ó", "o");
                idx.putIfAbsent(normKey, pos);
            }
            // also ensure any normalizedMap entries are present
            for (Map.Entry<String, Integer> e : normalizedMap.entrySet()) {
                idx.putIfAbsent(e.getKey(), e.getValue());
            }

            // Mostrar en logs el mapeo final de índices (clave -> posición) para depuración
            System.out.println("[FileProcessingService] mapeo final de índices: " + idx);

            // --- Robustificación adicional: buscar y fijar índices canónicos críticos por fragmentos ---
            // Esto evita que variantes de encabezados provoquen desplazamientos de columnas.
            BiConsumer<String[], String> ensureByFragments = (candidatesAndFallback, canonical) -> {
                // candidatesAndFallback: lista de fragmentos; si alguno coincide en normalizedMap o headerMap lo asigna
                for (String cand : candidatesAndFallback) {
                    String c = cand.toLowerCase();
                    // buscar en normalizedMap
                    for (Map.Entry<String, Integer> nm : normalizedMap.entrySet()) {
                        if (nm.getKey().contains(c)) {
                            idx.put(canonical, nm.getValue());
                            return;
                        }
                    }
                    // buscar en headerMap keys (lower-case)
                    for (Map.Entry<String, Integer> hm : headerMap.entrySet()) {
                        if (hm.getKey().contains(c)) {
                            idx.put(canonical, hm.getValue());
                            return;
                        }
                    }
                }
            };

            // definir fragmentos para columnas críticas
            ensureByFragments.accept(new String[]{"instcod", "instcodinstituc", "inst_cod", "cod_institucion", "codinstitucion"}, "inst_cod_institucion");
            ensureByFragments.accept(new String[]{"instnombre", "instnombreinstitucion", "inst_nombre", "nombre_institucion", "nombreinstitucion"}, "inst_nombre_institucion");
            ensureByFragments.accept(new String[]{"estu_snies", "snies", "estu_snies_prgmacademico", "sniesprgmacademico"}, "estu_snies_prgmacademico");

            // También asegurar nombres de puntajes/ppercentiles si existe alguna variación conocida
            ensureByFragments.accept(new String[]{"mod_competen_ciudada_punt", "mod_competen_ciudada_punt", "modcompetenciapunt", "mod_competen"}, "mod_competen_ciudada_punt");
            ensureByFragments.accept(new String[]{"mod_comuni_escrita_punt", "mod_comuni_escrita_punt", "modcomunipunt"}, "mod_comuni_escrita_punt");
            ensureByFragments.accept(new String[]{"mod_ingles_punt", "mod_ingles_punt", "modinglespunt"}, "mod_ingles_punt");
            ensureByFragments.accept(new String[]{"mod_lectura_critica_punt", "mod_lectura_critica_punt", "modlecturapunt"}, "mod_lectura_critica_punt");
            ensureByFragments.accept(new String[]{"mod_razona_cuantitat_punt", "mod_razona_cuantitat_punt", "modrazonapunt"}, "mod_razona_cuantitat_punt");
            ensureByFragments.accept(new String[]{"percentil_global", "percentilglobal"}, "percentil_global");
            ensureByFragments.accept(new String[]{"percentil_nbc", "percentilnbc"}, "percentil_nbc");
            ensureByFragments.accept(new String[]{"punt_global", "puntglobal"}, "punt_global");

            System.out.println("[FileProcessingService] Encabezados (map): " + headerMap.keySet());


            // empezar a leer desde la siguiente línea (los datos)
            String line = br.readLine();
            int rowNum = 0;
            while (line != null) {
                // ignorar líneas completamente vacías (saltos de línea) sin hacer warnings
                if (line.trim().isEmpty()) {
                    line = br.readLine();
                    continue;
                }
                String[] cols = splitLine(line, detectedDelimiter);
                // Si la línea tiene menos columnas que el header, rellenar con cadenas vacías para mantener índices
                if (cols.length < headers.length) {
                    String[] padded = new String[headers.length];
                    Arrays.fill(padded, "");
                    System.arraycopy(cols, 0, padded, 0, cols.length);
                    cols = padded;
                }
                ExternalGeneralResult r = new ExternalGeneralResult();
                // normalizar periodo para almacenar solo el año (ej. 20121 -> 2012)
                Integer periodoParsed = parsePeriodoAsYear(get(cols, idx, "periodo"));
                if (periodoParsed == null) {
                    // registrar y saltar esta línea en vez de lanzar excepción para no interrumpir la carga masiva
                    System.err.println("Warning: línea ignorada por periodo inválido o ausente: " + line);
                    line = br.readLine();
                    continue;
                }
                r.setPeriodo(periodoParsed);
                r.setEstConsecutivo(get(cols, idx, "est_consecutivo"));
                if (r.getEstConsecutivo() == null) {
                    System.err.println("Warning: fila ignorada por est_consecutivo ausente (periodo=" + r.getPeriodo() + ")");
                    line = br.readLine();
                    continue;
                }

                // Strings: mantener null si faltan
                r.setEstuDiscapacidad(get(cols, idx, "estu_discapacidad"));
                // Normalizar y resolver departamento/ciudad: si viene código numérico lo usamos; si viene nombre, buscamos/creamos
                String rawDept = get(cols, idx, "estu_inst_departamento");
                Integer deptId = resolveDepartamentoId(rawDept);
                r.setEstuInstDepartamento(deptId);
                String rawCiudad = get(cols, idx, "estu_inst_municipio");
                Integer ciudadId = resolveCiudadId(rawCiudad);
                r.setEstuInstMunicipio(ciudadId);
                r.setEstuNucleoPregrado(get(cols, idx, "estu_nucleo_pregrado"));
                r.setEstuPrgmAcademico(get(cols, idx, "estu_prgm_academico"));
                // estu_snies_prgmacademico es un código numérico -> parsear a Integer
                r.setEstuSniesPrgmacademico(parseInteger(get(cols, idx, "estu_snies_prgmacademico")));

                // Código de institución: usar null si ausente (no forzar 0)
                r.setInstCodInstitucion(parseInteger(get(cols, idx, "inst_cod_institucion")));
                r.setInstNombreInstitucion(get(cols, idx, "inst_nombre_institucion"));

                // PUNTAJES / PERCENTILES: parsear como BigDecimal y dejar null si ausente o inválido
                r.setModCompetenCiudadaPnal(parseBigDecimal(get(cols, idx, "mod_competen_ciudada_pnal")));
                r.setModCompetenCiudadaPnbc(parseBigDecimal(get(cols, idx, "mod_competen_ciudada_pnbc")));
                r.setModCompetenCiudadaPunt(parseBigDecimal(get(cols, idx, "mod_competen_ciudada_punt")));

                r.setModComuniEscritaPnal(parseBigDecimal(get(cols, idx, "mod_comuni_escrita_pnal")));
                r.setModComuniEscritaPnbc(parseBigDecimal(get(cols, idx, "mod_comuni_escrita_pnbc")));
                r.setModComuniEscritaPunt(parseBigDecimal(get(cols, idx, "mod_comuni_escrita_punt")));

                r.setModInglesPnal(parseBigDecimal(get(cols, idx, "mod_ingles_pnal")));
                r.setModInglesPnbc(parseBigDecimal(get(cols, idx, "mod_ingles_pnbc")));
                r.setModInglesPunt(parseBigDecimal(get(cols, idx, "mod_ingles_punt")));

                r.setModLecturaCriticaPnal(parseBigDecimal(get(cols, idx, "mod_lectura_critica_pnal")));
                r.setModLecturaCriticaPnbc(parseBigDecimal(get(cols, idx, "mod_lectura_critica_pnbc")));
                r.setModLecturaCriticaPunt(parseBigDecimal(get(cols, idx, "mod_lectura_critica_punt")));

                r.setModRazonaCuantitativoPnal(parseBigDecimal(get(cols, idx, "mod_razona_cuantitativo_pnal")));
                r.setModRazonaCuantitativoPnbc(parseBigDecimal(get(cols, idx, "mod_razona_cuantitativo_pnbc")));
                r.setModRazonaCuantitatPunt(parseBigDecimal(get(cols, idx, "mod_razona_cuantitat_punt")));

                r.setPercentilGlobal(parseBigDecimal(get(cols, idx, "percentil_global")));
                r.setPercentilNbc(parseBigDecimal(get(cols, idx, "percentil_nbc")));
                r.setPuntGlobal(parseBigDecimal(get(cols, idx, "punt_global")));

                r.setCreatedAt(OffsetDateTime.now());
                buffer.add(r);

                // Loguear las primeras 5 filas para depuración
                if (rowNum < 5) {
                    System.out.println("[FileProcessingService] preview row " + rowNum + ": periodo=" + r.getPeriodo() + ", estu_consec=" + r.getEstConsecutivo() + ", estu_discapacidad='" + r.getEstuDiscapacidad() + "', inst_cod_institucion=" + r.getInstCodInstitucion() + ", inst_nombre_institucion='" + r.getInstNombreInstitucion() + "'");
                }
                rowNum++;

                if (buffer.size() >= BATCH_SIZE_GENERAL) {
                    // Normalizar valores obligatorios antes de guardar (solo asegurar periodo y est_consecutivo)
                    for (ExternalGeneralResult gr : buffer) normalizeGeneralResult(gr);
                    List<ExternalGeneralResult> saved = generalRepo.saveAll(new ArrayList<>(buffer));
                    savedAll.addAll(saved);
                    buffer.clear();
                }
                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                for (ExternalGeneralResult gr : buffer) normalizeGeneralResult(gr);
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

            boolean hasHeader = looksLikeHeader(firstLine, new String[]{"estu_consecutivo", "result_nombreprueba"});
            String[] headers = hasHeader ? splitLine(firstLine) : null;
            Map<String, Integer> idx = new HashMap<>();
            if (headers != null) for (int i = 0; i < headers.length; i++) idx.put(headers[i].trim().toLowerCase(), i);

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

            boolean hasHeader = looksLikeHeader(firstLine, new String[]{"tipo de documento", "documento"});
            String[] headers = hasHeader ? splitLine(firstLine) : null;
            Map<String, Integer> idx = new HashMap<>();
            if (headers != null) for (int i = 0; i < headers.length; i++) idx.put(headers[i].trim().toLowerCase(), i);

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

    private String get(String[] cols, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key.toLowerCase());
        if (i == null || i < 0 || i >= cols.length) {
            for (Map.Entry<String, Integer> e : idx.entrySet()) {
                String k = e.getKey().replace(" ", "").replace("í", "i").replace("ó", "o");
                String kk = key.toLowerCase().replace(" ", "").replace("í", "i").replace("ó", "o");
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
        String n2 = name.replace("í", "i").replace("ó", "o").trim();
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
        String n2 = name.replace("í", "i").replace("ó", "o").trim();
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

    // Asegura que los campos que la BD espera no nulos tengan un valor por defecto
    private void normalizeGeneralResult(ExternalGeneralResult r) {
        // Ahora no forzamos valores numéricos por defecto: si falta, quedará null en BD
        if (r.getEstConsecutivo() == null) return; // sanity
        // Dejar strings como están (null si faltan) — el usuario pidió que faltantes sean NULL
        // No sobrescribir instCodInstitucion, ni puntajes si son null
    }
}
