package services;

import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.transaction.annotation.Transactional;
import model.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;
import repository.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Service
public class FileProcessingService {
    private final ExternalGeneralResultRepository generalRepo; //solo tiene una asignación (final)
    private final ExternalModuleResultRepository externalModuleRepo; // repo para resultado_modulo_externo
    private final ExternalSpecificResultRepository externalSpecificRepo; // repo para resultado_especifico_externo (nueva tabla)
    private final InternalResultRepository internalRepo;
    private final CiudadRepository ciudadRepo;
    private final DepartamentoRepository departamentoRepo;
    private final ModuloRepository moduloRepo;
    private final InternaModuleResultRepository moduleResultRepo;

    // cache para módulos (nombre en mayúsculas -> id)
    private final Map<String, Integer> moduloCache = new ConcurrentHashMap<>();

    // Cache para ciudades (nombre en mayúsculas -> id)
    private final Map<String, Integer> ciudadCache = new ConcurrentHashMap<>();

    //Static: solo existe una copia compartida por todas las instancias de la clase
    private static final int BATCH_SIZE_GENERAL = 2000; //
    private static final int BATCH_SIZE_SPEC = 500;
    private static final int BATCH_SIZE_INTERNAL = 1000;

    public FileProcessingService(ExternalGeneralResultRepository generalRepo,
                                 ExternalModuleResultRepository externalModuleRepo,
                                 ExternalSpecificResultRepository externalSpecificRepo,
                                 InternalResultRepository internalRepo,
                                 CiudadRepository ciudadRepo,
                                 DepartamentoRepository departamentoRepo,
                                 ModuloRepository moduloRepo,
                                 InternaModuleResultRepository moduleResultRepo) {
        this.generalRepo = generalRepo;
        this.externalModuleRepo = externalModuleRepo;
        this.externalSpecificRepo = externalSpecificRepo;
        this.internalRepo = internalRepo;
        this.ciudadRepo = ciudadRepo;
        this.departamentoRepo = departamentoRepo;
        this.moduloRepo = moduloRepo;
        this.moduleResultRepo = moduleResultRepo;

        // precargar cache de módulos existentes para evitar consultas repetidas
        try {
            Iterable<Modulo> all = this.moduloRepo.findAll();
            if (all != null) {
                for (Modulo m : all) {
                    if (m != null && m.getNombre() != null) {
                        moduloCache.put(m.getNombre().trim().toUpperCase(), m.getIdModulo());
                    }
                }
            }
        } catch (Exception e) {
            // si la carga falla (p. ej. DB no disponible en construcción), no interrumpir la app
            System.err.println("Warning: no se pudo precargar cache de modulos: " + e.getMessage());
        }

        // Precargar caché de ciudades para optimizar
        try {
            for (Ciudad c : this.ciudadRepo.findAll()) {
                if (c != null && c.getNombre() != null) {
                    ciudadCache.put(normalizeCityName(c.getNombre()), c.getIdCiudad());
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: no se pudo precargar cache de ciudades: " + e.getMessage());
        }
    }

    // Split que usa solo ';' o ','
    private String[] splitLine(String line) {
        if (line == null) return new String[0];
        String delimiter = ";";
        if (line.contains(";")) delimiter = ";";
        else if (line.contains(",")) delimiter = ",";
        return line.split(java.util.regex.Pattern.quote(delimiter), -1); //devuelve todas las columnas, incluso vacías
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
        for (String t : requiredTokens) if (!low.contains(t)) return false; // si falta algún token requerido return false;
        return true;
    }

    //Detecta delimitador y headers, construye índices tolerantes a variaciones, parsea cada fila a ExternalGeneralResult, setea createdAt, agrupa en buffer,
    // normaliza al final y persiste mediante generalRepo.saveAll(...) en batches. Devuelve la lista de entidades persistidas.
    @Transactional //
    public List<ExternalGeneralResult> parseAndSaveGeneral(MultipartFile file) throws Exception {
        List<ExternalGeneralResult> savedAll = new ArrayList<>();
        List<ExternalGeneralResult> buffer = new ArrayList<>(BATCH_SIZE_GENERAL);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = br.readLine();
            if (firstLine == null) return savedAll;

            // Depuración: mostrar header leído y detectar delimitador (coherente con TxtReaderService)
            // Normalizar primera línea: quitar BOM y espacios, y crear versión en minúsculas para reconocimiento
            String cleanedFirstLine = firstLine.replace("\uFEFF", "").trim(); // quitar BOM y espacios: Byte Order Mark carácter Unicode U+FEFF que algunos editores o exportadores añaden al inicio de un fichero para indicar la codificación/orden de bytes
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
                headerMap.put(lower, i); // clave en minúsculas y sin espacios alrededor sobreescribe
                String norm = lower.replace(" ", "").replace("-","\u2011").replace("_", "").replace("í", "i").replace("ó", "o");
                normalizedMap.put(norm, i); // clave normalizada sin espacios, guiones, tildes junto con la posición
            }

            // Intentar mapear columnas mínimas con tolerancia (variantes de nombres)
            Integer idxPeriodo = null;
            Integer idxEstu = null;
            if (normalizedMap.containsKey("periodo")) idxPeriodo = normalizedMap.get("periodo");
            else {
                for (String k : normalizedMap.keySet()) // buscar cualquier encabezado que contenga 'periodo'
                    if (k.contains("periodo")) {
                        idxPeriodo = normalizedMap.get(k); //
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

                String normKey = rawKey.replace(" ", "").replace("-", "").replace("_", "").replace("í", "i").replace("ó", "o");
                idx.putIfAbsent(normKey, pos);
            }
            for (Map.Entry<String, Integer> e : normalizedMap.entrySet()) {
                idx.putIfAbsent(e.getKey(), e.getValue());
            } // obtener columnas aun cuando el header del archivo tenga pequenas diferencias de formato o acentuación.

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

            // También asegurar nombres de puntajes/percentiles si existe alguna variación conocida
            ensureByFragments.accept(new String[]{"mod_competen_ciudada_punt", "mod_competen_ciudada_punt", "modcompetenciapunt", "mod_competen"}, "mod_competen_ciudada_punt");
            ensureByFragments.accept(new String[]{"mod_comuni_escrita_punt", "mod_comuni_escrita_punt", "modcomunipunt"}, "mod_comuni_escrita_punt");
            ensureByFragments.accept(new String[]{"mod_ingles_punt", "mod_ingles_punt", "modinglespunt"}, "mod_ingles_punt");
            ensureByFragments.accept(new String[]{"mod_lectura_critica_punt", "mod_lectura_critica_punt", "modlecturapunt"}, "mod_lectura_critica_punt");
            ensureByFragments.accept(new String[]{"mod_razona_cuantitat_punt", "mod_razona_cuantitat_punt", "modrazonapunt"}, "mod_razona_cuantitat_punt");
            ensureByFragments.accept(new String[]{"percentil_global", "percentilglobal"}, "percentil_global");
            ensureByFragments.accept(new String[]{"percentil_nbc", "percentilnbc"}, "percentil_nbc");
            ensureByFragments.accept(new String[]{"punt_global", "puntglobal"}, "punt_global");

            System.out.println("[FileProcessingService] Encabezados (map): " + headerMap.keySet());

            // Detectar qué prefijos de módulo están presentes en el archivo (aunque las celdas estén vacías)
            // Esto nos permitirá crear filas en resultado_modulo_externo con puntaje = NULL cuando las columnas existen pero las celdas están vacías.
            List<String> modulePrefixes = Arrays.asList("mod_competen_ciudada", "mod_comuni_escrita", "mod_ingles", "mod_lectura_critica", "mod_razona_cuantitat"); // prefijos esperados de módulos
            Set<String> presentModulePrefixes = new HashSet<>(); // prefijos de módulos detectados en el archivo
            for (String pref : modulePrefixes) {
                String compactPref = pref.replace("_", "");
                boolean found = false;
                for (String h : headerMap.keySet()) { //revisar si existe en headerMap o normalizedMap
                    if (h.contains(pref) || h.replace("_", "").contains(compactPref)) { found = true; break; } // buscar coincidencia parcial
                }
                if (!found) {
                    for (String h : normalizedMap.keySet()) {
                        if (h.contains(compactPref)) { found = true; break; } //
                    }
                }
                if (found) presentModulePrefixes.add(pref);
            }

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
                // Revertir: mantener departamento/ciudad como texto (no normalizamos a id)
                String rawDept = get(cols, idx, "estu_inst_departamento");
                r.setEstuInstDepartamento(rawDept);
                String rawCiudad = get(cols, idx, "estu_inst_municipio");
                r.setEstuInstMunicipio(rawCiudad);
                r.setEstuNucleoPregrado(get(cols, idx, "estu_nucleo_pregrado"));
                r.setEstuPrgmAcademico(get(cols, idx, "estu_prgm_academico"));
                // estu_snies_prgmacademico: dejar como Integer para coincidir con BD
                r.setEstuSniesPrgmacademico(parseInteger(get(cols, idx, "estu_snies_prgmacademico")));

                // Código de institución: dejar como Integer si es posible
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

                if (buffer.size() >= BATCH_SIZE_GENERAL) {
                    for (ExternalGeneralResult gr : buffer) normalizeGeneralResult(gr);
                    List<ExternalGeneralResult> saved = generalRepo.saveAll(new ArrayList<>(buffer));
                    List<ExternalModuleResult> modulesToSave = new ArrayList<>();
                    for (ExternalGeneralResult gr : saved) {
                        modulesToSave.addAll(extractModuleRows(gr, presentModulePrefixes));
                    }
                    if (!modulesToSave.isEmpty()) externalModuleRepo.saveAll(modulesToSave);
                    savedAll.addAll(saved);
                    buffer.clear();
                }
                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                for (ExternalGeneralResult gr : buffer) normalizeGeneralResult(gr);
                List<ExternalGeneralResult> saved = generalRepo.saveAll(buffer);
                List<ExternalModuleResult> modulesToSave = new ArrayList<>();
                for (ExternalGeneralResult gr : saved) {
                    modulesToSave.addAll(extractModuleRows(gr, presentModulePrefixes));
                }
                if (!modulesToSave.isEmpty()) externalModuleRepo.saveAll(modulesToSave);
                savedAll.addAll(saved);
                buffer.clear();
            }
        }
        return savedAll;
    }

    //Se guarda los módulos normalizados
    @Transactional
    public List<ExternalModuleResult> parseAndSaveSpecifics(MultipartFile file, Integer periodo) throws Exception {
        List<ExternalModuleResult> savedAll = new ArrayList<>();
        List<ExternalModuleResult> buffer = new ArrayList<>(BATCH_SIZE_SPEC);

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
                ExternalModuleResult r = new ExternalModuleResult();
                String estConsec = get(cols, idx, "estu_consecutivo");
                String prueba = get(cols, idx, "result_nombreprueba");
                Integer moduloId = resolveOrCreateModuloId(prueba);
                r.setModuloId(moduloId);
                r.setResultPuntaje(parseBigDecimal(get(cols, idx, "result_puntaje")));
                r.setPercentilNacional(parseInteger(get(cols, idx, "percentil_nacional")));
                r.setPercentilNbc(parseInteger(get(cols, idx, "percentil_nbc")));
                buffer.add(r);

                if (buffer.size() >= BATCH_SIZE_SPEC) {
                    List<ExternalModuleResult> saved = externalModuleRepo.saveAll(new ArrayList<>(buffer));
                    savedAll.addAll(saved);
                    buffer.clear();
                }
                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                List<ExternalModuleResult> saved = externalModuleRepo.saveAll(buffer);
                savedAll.addAll(saved);
                buffer.clear();
            }
        }
        return savedAll;
    }

    @Transactional
    public List<ExternalSpecificResult> parseAndSaveSpecificsResults(MultipartFile file, int periodo) throws Exception {
        List<ExternalSpecificResult> savedAll = new ArrayList<>();
        List<ExternalSpecificResult> buffer = new ArrayList<>(BATCH_SIZE_SPEC);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = br.readLine();
            if (firstLine == null) return savedAll;

            boolean hasHeader = looksLikeHeader(firstLine, new String[]{"estu_consecutivo", "result_nombreprueba", "result_puntaje"});
            String[] headers = hasHeader ? splitLine(firstLine) : null;
            Map<String, Integer> idx = new HashMap<>();
            if (headers != null) {
                for (int i = 0; i < headers.length; i++) idx.put(headers[i].trim().toLowerCase(), i);
            } else {
                idx.put("estu_consecutivo", 0);
                idx.put("result_nombreprueba", 1);
                idx.put("result_puntaje", 2);
            }

            String line = hasHeader ? br.readLine() : firstLine;

            while (line != null) {
                if (line.trim().isEmpty()) { line = br.readLine(); continue; }

                String[] cols = splitLine(line);
                if (headers != null && cols.length < headers.length) {
                    String[] tmp = new String[headers.length];
                    System.arraycopy(cols, 0, tmp, 0, cols.length);
                    for (int i = cols.length; i < tmp.length; i++) tmp[i] = "";
                    cols = tmp;
                }

                String estConsec = get(cols, idx, "estu_consecutivo");
                String prueba = get(cols, idx, "result_nombreprueba");
                String puntStr = get(cols, idx, "result_puntaje");

                Integer moduloId = resolveOrCreateModuloId(prueba);

                Integer puntInt = null;
                if (puntStr != null) {
                    BigDecimal bd = parseBigDecimal(puntStr);
                    if (bd != null) puntInt = (int) Math.round(bd.doubleValue());
                }

                if (estConsec == null || moduloId == null || puntInt == null) {
                    System.err.println("Warning: fila especifica ignorada por datos incompletos: estuConsec=" + estConsec + " moduloId=" + moduloId + " punt=" + puntStr);
                } else {
                    ExternalSpecificResult rs = new ExternalSpecificResult();
                    rs.setEstuConsecutivo(estConsec);
                    rs.setResultNombrePrueba(moduloId);
                    rs.setResultPuntaje(puntInt);
                    buffer.add(rs);
                }

                if (buffer.size() >= BATCH_SIZE_SPEC) {
                    List<ExternalSpecificResult> saved = externalSpecificRepo.saveAll(new ArrayList<>(buffer));
                    savedAll.addAll(saved);
                    buffer.clear();
                }

                line = br.readLine();
            }

            if (!buffer.isEmpty()) {
                List<ExternalSpecificResult> saved = externalSpecificRepo.saveAll(buffer);
                savedAll.addAll(saved);
                buffer.clear();
            }
        }

        return savedAll;
    }

    @Transactional
    public List<InternalResult> parseAndSaveInternal(MultipartFile file, int periodo) throws Exception {
        List<InternalResult> savedAll = new ArrayList<>();
        List<InternalResult> buffer = new ArrayList<>(BATCH_SIZE_INTERNAL);
        List<TempModuleResult> moduleTempBuffer = new ArrayList<>();

        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            // Lógica para archivos Excel
            try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                // Procesar todas las hojas del workbook (cada hoja puede representar un programa)
                int sheets = workbook.getNumberOfSheets();
                for (int sh = 0; sh < sheets; sh++) {
                    Sheet shSheet = workbook.getSheetAt(sh);
                    if (shSheet == null) continue;

                    Map<String, Integer> headerMap = new HashMap<>();
                    Row headerRow = shSheet.getRow(0);
                    if (headerRow != null) {
                        for (Cell cell : headerRow) {
                            String header = getCellValueAsString(cell);
                            if (header != null && !header.trim().isEmpty()) {
                                String normalizedHeader = header.trim().toLowerCase()
                                        .replace(" ", "")
                                        .replace("í", "i")
                                        .replace("ó", "o")
                                        .replace("ú", "u")
                                        .replace("é", "e")
                                        .replace("á", "a");
                                headerMap.put(normalizedHeader, cell.getColumnIndex());
                            }
                        }
                    }

                    boolean isMerged = isMergedCell(shSheet,shSheet.getRow(0).getCell(0)) || isMergedCell(shSheet,shSheet.getRow(1).getCell(0));

                    class IlegalExcept extends RuntimeException{
                        public IlegalExcept(String message){
                            super(message);
                        }
                    }

                    if(isMerged){
                        throw new IlegalExcept("Las columnas de encabezado están combinadas.");
                    }

                    // Procesar filas de esta hoja
                    for (int i = 1; i <= shSheet.getLastRowNum(); i++) {
                        Row row = shSheet.getRow(i);
                        if (row == null) continue;

                        InternalResult r = new InternalResult();
                        r.setPeriodo(periodo);
                        r.setTipoDocumento(getCellValueByHeader(row, headerMap, "tipodedocumento"));
                        String documentoRaw = getCellValueByHeader(row, headerMap, "documento");
                        r.setDocumento(parseLongOrNull(documentoRaw));
                        r.setNombre(getCellValueByHeader(row, headerMap, "nombre"));
                        r.setNumeroRegistro(getCellValueByHeader(row, headerMap, "numeroderegistro"));
                        r.setTipoEvaluado(getCellValueByHeader(row, headerMap, "tipodeevaluado"));
                        r.setSniesPrograma(getCellValueByHeader(row, headerMap, "sniesprogramaacademico"));
                        r.setPrograma(getCellValueByHeader(row, headerMap, "programa"));
                        // Resolver la ciudad por nombre y crearla si no existe
                        String ciudadRaw = getCellValueByHeader(row, headerMap, "ciudad");
                        Integer ciudadIdResolved = resolveCiudadId(ciudadRaw);
                        r.setCiudadId(ciudadIdResolved);
                        r.setGrupoReferencia(getCellValueByHeader(row, headerMap, "grupodereferencia"));
                        r.setPuntajeGlobal(parseInteger(getCellValueByHeader(row, headerMap, "puntajeglobal")));
                        r.setPercentilNacionalGlobal(parseInteger(getCellValueByHeader(row, headerMap, "percentilnacionalglobal")));
                        r.setPercentilGrupoReferencia(parseInteger(getCellValueByHeader(row, headerMap, "percentilgrupodereferencia")));
                        r.setCreatedAt(OffsetDateTime.now());

                        int bufferIndex = buffer.size();
                        buffer.add(r);

                        String moduloNombre = getCellValueByHeader(row, headerMap, "módulo");
                        if (moduloNombre == null) moduloNombre = getCellValueByHeader(row, headerMap, "modulo");
                        Integer puntajeModulo = parseInteger(getCellValueByHeader(row, headerMap, "puntajemodulo"));
                        Integer percentilNacModulo = parseInteger(getCellValueByHeader(row, headerMap, "percentilnacionalmodulo"));
                        Integer percentilGrupoModulo = parseInteger(getCellValueByHeader(row, headerMap, "percentilgrupodereferenciamodulo"));

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
                            linkAndSaveModuleResults(saved, moduleTempBuffer);
                            savedAll.addAll(saved);
                            buffer.clear();
                            moduleTempBuffer.clear();
                        }
                    }

                    // Al terminar de procesar todas las hojas, persistir cualquier resto en buffer
                    if (!buffer.isEmpty()) {
                        List<InternalResult> saved = internalRepo.saveAll(new ArrayList<>(buffer));
                        // los índices en moduleTempBuffer fueron calculados con respecto a `buffer` antes del guardado,
                        // por eso al pasar `saved` (mismo orden) se puede usar directamente internalIndex
                        linkAndSaveModuleResults(saved, moduleTempBuffer);
                        savedAll.addAll(saved);
                        buffer.clear();
                        moduleTempBuffer.clear();
                    }

                }
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
    private Long parseLongOrNull(String value) {
        if (value == null) return null;
        String s = value.trim();
        if (s.isEmpty()) return null;
        // eliminar espacios no separables y otros caracteres comunes
        s = s.replace("\u00A0", "").replace(" ", "").replace(".", ""); // quitar puntos de formato miles
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            try {
                // intentar con decimal y redondear
                double d = Double.parseDouble(s.replace(',', '.'));
                return (long) Math.round(d);
            } catch (Exception ex) {
                // no se pudo parsear
                return null;
            }
        }
    }

    // Nuevo helper: obtener el valor de una celda como String manejando tipos comunes de Apache POI
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        DataFormatter df = new DataFormatter();
        try {
            switch (cell.getCellType()) {
                case STRING:
                    String s = cell.getStringCellValue();
                    return s == null ? null : s.trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // devolver la representación por defecto de la fecha (se puede adaptar si se requiere formato específico)
                        return cell.getDateCellValue().toString();
                    }
                    return df.formatCellValue(cell).trim();
                case BOOLEAN:
                    return Boolean.toString(cell.getBooleanCellValue());
                case FORMULA:
                    // DataFormatter aplicará la fórmula y devolverá el valor formateado
                    return df.formatCellValue(cell).trim();
                case BLANK:
                    return null;
                default:
                    return df.formatCellValue(cell).trim();
            }
        } catch (Throwable t) {
            return null;
        }
    }

    // Obtener el valor de una celda por nombre de encabezado (headerMap puede contener claves normalizadas o sin acentos)
    private String getCellValueByHeader(Row row, Map<String, Integer> headerMap, String headerName) {
        if (row == null || headerMap == null || headerName == null) return null;
        // intentar búsqueda directa por la clave tal cual (lower-case)
        Integer col = headerMap.get(headerName.toLowerCase());
        if (col == null) {
            // intentar versión normalizada: quitar espacios y normalizar acentos
            String want = headerName.toLowerCase().replace(" ", "").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("é", "e").replace("á", "a");
            for (Map.Entry<String, Integer> e : headerMap.entrySet()) {
                String key = e.getKey();
                if (key == null) continue;
                String k = key.toLowerCase().replace(" ", "").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("é", "e").replace("á", "a");
                if (k.equals(want)) { col = e.getValue(); break; }
            }
        }
        if (col == null) return null;
        Cell c = row.getCell(col);
        String v = getCellValueAsString(c);
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    // Nuevo método para normalizar nombres de ciudades de forma consistente
    private String normalizeCityName(String rawName) {
        if (rawName == null) return "";
        return java.text.Normalizer.normalize(rawName, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // Quita tildes y diacríticos
                .trim()
                .toUpperCase();
    }

    @Transactional(propagation = Propagation.REQUIRED) // Cambiado de REQUIRES_NEW a REQUIRED
    public Integer resolveCiudadId(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String original = raw.trim();

        // Si el valor parece un, id numérico, comprobar existencia y devolverlo solo si existe.
        Integer asInt = parseInteger(original);
        if (asInt != null) {
            try {
                if (ciudadRepo.existsById(asInt)) return asInt;
                else {
                    System.err.println("[FileProcessingService] Ciudad indicada por id no existe en DB: " + asInt + " (valor de entrada: '" + original + "')");
                    // continuar tratando original como nombre en vez de devolver este, id inválido
                }
            } catch (Exception e) {
                // si existe problema al comprobar, seguir y tratar como nombre
            }
        }

        String normalizedName = normalizeCityName(original);

        // 1. revisar cache
        Integer cached = ciudadCache.get(normalizedName);
        if (cached != null) {
            // comprobar que la ciudad aún existe (evita caché stale)
            try {
                if (ciudadRepo.existsById(cached)) return cached;
                else ciudadCache.remove(normalizedName);
            } catch (Exception e) {
                // si existe problema al comprobar, seguir y buscar en BD
            }
        }

        // 2. buscar en BD por nombre (ignore case)
        Optional<Ciudad> oc = ciudadRepo.findFirstByNombreIgnoreCase(original);
        if (!oc.isPresent()) {
            // intentar buscar por versión normalizada (sin tildes)
            for (Ciudad c : ciudadRepo.findAll()) {
                if (c.getNombre() != null && normalizeCityName(c.getNombre()).equals(normalizedName)) {
                    ciudadCache.put(normalizedName, c.getIdCiudad());
                    return c.getIdCiudad();
                }
            }
        } else {
            Integer id = oc.get().getIdCiudad();
            ciudadCache.put(normalizedName, id);
            return id;
        }

        // 3. Si no existe, crear la ciudad. Intentar asignar un departamento por defecto seguro.
        synchronized (ciudadCache) {
            if (ciudadCache.containsKey(normalizedName)) return ciudadCache.get(normalizedName);

            Ciudad nueva = new Ciudad();
            nueva.setNombre(original);
            // intentar asignar un departamento con id 1 si existe, si no buscar cualquier departamento o dejar null
            Integer deptId = null;
            try {
                if (departamentoRepo.count() > 0) {
                    // usar el primer departamento existente (evitar FK violación si id 1 no existe)
                    Optional<Departamento> any = Optional.empty();
                    for (Departamento d : departamentoRepo.findAll()) { any = Optional.of(d); break; }
                    if (any.isPresent()) deptId = any.get().getIdDepartamento().intValue();
                }
            } catch (Exception e) {
                // ignore
            }
            if (deptId != null) nueva.setIdDepartamento(deptId);
            try {
                Ciudad saved = ciudadRepo.saveAndFlush(nueva);
                ciudadCache.put(normalizedName, saved.getIdCiudad());
                System.out.println("[FileProcessingService] Ciudad creada: '" + original + "' -> id=" + saved.getIdCiudad());
                return saved.getIdCiudad();
            } catch (Exception e) {
                System.err.println("[FileProcessingService] Error al crear ciudad '" + original + "': " + e.getMessage());
                return null;
            }
        }
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
        String key = normalized.toUpperCase();

        Integer cached = moduloCache.get(key);
        if (cached != null) return cached;

        Optional<Modulo> om = moduloRepo.findFirstByNombreIgnoreCase(normalized);
        if (om.isPresent()) {
            Integer id = om.get().getIdModulo();
            moduloCache.put(key, id);
            return id;
        }

        synchronized (moduloCache) {
            Integer recheck = moduloCache.get(key);
            if (recheck != null) return recheck;

            om = moduloRepo.findFirstByNombreIgnoreCase(normalized);
            if (om.isPresent()) {
                Integer id = om.get().getIdModulo();
                moduloCache.put(key, id);
                return id;
            }

            Modulo nuevoModulo = new Modulo();
            nuevoModulo.setNombre(normalized);
            Modulo savedModulo = moduloRepo.save(nuevoModulo);
            Integer newId = savedModulo.getIdModulo();
            moduloCache.put(key, newId);
            return newId;
        }
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
        List<InternalModuleResult> toSave = new ArrayList<>();
        for (TempModuleResult t : tempModules) {
            if (t.internalIndex >= 0 && t.internalIndex < savedInternals.size()) {
                InternalResult related = savedInternals.get(t.internalIndex);
                if (related != null) {
                    InternalModuleResult rm = new InternalModuleResult();
                    rm.setInternoId(related.getDocumento());
                    Integer modId = resolveOrCreateModuloId(t.moduloNombre);
                    rm.setModuloId(modId);
                    rm.setPuntaje(t.puntaje);
                    rm.setPercentilNacional(t.percentilNacional);
                    rm.setPercentilGrupoReferencia(t.percentilGrupoReferencia);
                    toSave.add(rm);
                }
            }
        }
        if (!toSave.isEmpty()) {
            try {
                moduleResultRepo.saveAll(toSave);
            } catch (Exception e) {
                System.err.println("[FileProcessingService] Error guardando resultado_modulo_interno: " + e.getMessage());
                throw e;
            }
        }
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
    private void normalizeGeneralResult(ExternalGeneralResult r) { // llamado antes de guardar en BD
        // Ahora no forzamos valores numéricos por defecto: si falta, quedará null en BD
        if (r.getEstConsecutivo() == null) return; // no se puede normalizar sin est_consecutivo
        // Dejar strings como están (null si faltan) — el usuario pidió que faltantes sean NULL
        // No sobrescribir instCodInstitucion, ni puntajes si son null
    }

    // Helper: convierte BigDecimal a Integer redondeando, o devuelve null
    private Integer bigDecimalToInteger(BigDecimal b) {
        if (b == null) return null;
        try {
            return (int) Math.round(b.doubleValue());
        } catch (Exception e) { return null; }
    }

    // Construye filas de resultado_modulo_externo a partir de un ExternalGeneralResult.
    private List<ExternalModuleResult> extractModuleRows(ExternalGeneralResult gr, Set<String> presentPrefixes) {
        List<ExternalModuleResult> out = new ArrayList<>();
        if (gr == null) return out;
        // mapa de prefijos de columna base a nombre legible
        Map<String, String> prefixToName = new LinkedHashMap<>();
        prefixToName.put("mod_competen_ciudada", "COMPETENCIAS CIUDADANAS");
        prefixToName.put("mod_comuni_escrita", "COMUNICACION ESCRITA");
        prefixToName.put("mod_ingles", "INGLES");
        prefixToName.put("mod_lectura_critica", "LECTURA CRITICA");
        prefixToName.put("mod_razona_cuantitat", "RAZONA CANTITATIVO");

        for (Map.Entry<String, String> e : prefixToName.entrySet()) {
            String pref = e.getKey();
            String name = e.getValue();

            BigDecimal punt = null;
            BigDecimal pnal = null;
            BigDecimal pnbc = null;
            try {
                // obtener mediante los getters de ExternalGeneralResult (nombres concretos)
                switch (pref) {
                    case "mod_competen_ciudada":
                        punt = gr.getModCompetenCiudadaPunt();
                        pnal = gr.getModCompetenCiudadaPnal();
                        pnbc = gr.getModCompetenCiudadaPnbc();
                        break;
                    case "mod_comuni_escrita":
                        punt = gr.getModComuniEscritaPunt();
                        pnal = gr.getModComuniEscritaPnal();
                        pnbc = gr.getModComuniEscritaPnbc();
                        break;
                    case "mod_ingles":
                        punt = gr.getModInglesPunt();
                        pnal = gr.getModInglesPnal();
                        pnbc = gr.getModInglesPnbc();
                        break;
                    case "mod_lectura_critica":
                        punt = gr.getModLecturaCriticaPunt();
                        pnal = gr.getModLecturaCriticaPnal();
                        pnbc = gr.getModLecturaCriticaPnbc();
                        break;
                    case "mod_razona_cuantitat":
                        punt = gr.getModRazonaCuantitatPunt();
                        pnal = gr.getModRazonaCuantitativoPnal();
                        pnbc = gr.getModRazonaCuantitativoPnbc();
                        break;
                }
            } catch (Throwable ex) {
                // ignore getter errors
            }

            boolean columnsPresent = (presentPrefixes != null && presentPrefixes.contains(pref));
            if (!columnsPresent && punt == null && pnal == null && pnbc == null) continue;

            ExternalModuleResult mr = new ExternalModuleResult();
            mr.setExternaId(gr.getId());

            Integer modId = resolveOrCreateModuloId(name);
            mr.setModuloId(modId);
            mr.setResultPuntaje(punt);
            mr.setPercentilNacional(bigDecimalToInteger(pnal));
            mr.setPercentilNbc(bigDecimalToInteger(pnbc));

            out.add(mr);
        }
        return out;
    }

    public static boolean isMergedCell(Sheet sheet, Cell cell) {
        if (sheet == null || cell == null) return false;
        int r = cell.getRowIndex();
        int c = cell.getColumnIndex();
        for (CellRangeAddress range : sheet.getMergedRegions()) {
            if (range.isInRange(r, c)) {
                return true;
            }
        }
        return false;
    }
}
