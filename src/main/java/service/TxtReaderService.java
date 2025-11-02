package service;

import model.ExternalGeneralResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TxtReaderService {

    private static final Logger log = LoggerFactory.getLogger(TxtReaderService.class);
    private static final String DEFAULT_DELIMITER = ";";

    /*
     -------------- NOTAS SOBRE CAMBIOS EN LA BD (guardar estas sentencias en un sitio seguro) --------------
     Los cambios recomendados en la base de datos para que los ficheros se importen sin violar constraints
     (se pueden aplicar con pgAdmin o psql). Aquí dejo las sentencias sugeridas:

     -- cambiar tipos para soportar decimales (ejemplo según campos usados en el modelo)
     ALTER TABLE saberpro.resultado_externo
       ALTER COLUMN mod_competen_ciudada_punt TYPE numeric(10,7) USING mod_competen_ciudada_punt::numeric,
       ALTER COLUMN mod_comuni_escrita_punt TYPE numeric(10,7) USING mod_comuni_escrita_punt::numeric,
       ALTER COLUMN mod_ingles_punt TYPE numeric(10,7) USING mod_ingles_punt::numeric,
       ALTER COLUMN mod_lectura_critica_punt TYPE numeric(10,7) USING mod_lectura_critica_punt::numeric,
       ALTER COLUMN mod_razona_cuantitat_punt TYPE numeric(10,7) USING mod_razona_cuantitat_punt::numeric,
       ALTER COLUMN percentil_global TYPE numeric(5,2) USING percentil_global::numeric,
       ALTER COLUMN percentil_nbc TYPE numeric(5,2) USING percentil_nbc::numeric,
       ALTER COLUMN punt_global TYPE numeric(6,2) USING punt_global::numeric;

     -- permitir NULL en columnas que el data provider a veces omite (ejemplo)
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN estu_discapacidad DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN inst_cod_institucion DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN inst_nombre_institucion DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN mod_competen_ciudada_punt DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN mod_comuni_escrita_punt DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN mod_ingles_punt DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN mod_lectura_critica_punt DROP NOT NULL;
     ALTER TABLE saberpro.resultado_externo ALTER COLUMN mod_razona_cuantitat_punt DROP NOT NULL;

     -- si se desea mantener integridad, en lugar de DROP NOT NULL se puede insertar un valor por defecto
     -- UPDATE saberpro.resultado_externo SET inst_cod_institucion = NULL WHERE inst_cod_institucion = 0;

     -------------------------------------------------------------------------------------------------------
     */

    /**
     * Lee un InputStream de un archivo .txt (delimitado por ; o ,) y lo convierte
     * a una lista de DTOs, mapeando solo las columnas de interés.
     */
    public List<ExternalGeneralResult> readTxtFile(InputStream inputStream) {
        List<ExternalGeneralResult> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // 1. Leer la primera línea (el encabezado)
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("El archivo está vacío.");
            }

            // 1.a Detectar delimitador (prioridad: ; ,)
            String delimiter = detectDelimiter(headerLine);

            // Depuración: mostrar header y delimitador detectado en consola
            log.debug("[TxtReaderService] Header detectado: '{}'", headerLine);
            log.debug("[TxtReaderService] Delimitador detectado: '{}'", delimiter);

            // 2. Crear un mapa de los encabezados que SÍ nos interesan (normalizados en minúsculas)
            Map<String, Integer> headerMap = new HashMap<>();
            String[] headers = headerLine.split(java.util.regex.Pattern.quote(delimiter), -1);
            for (int i = 0; i < headers.length; i++) {
                String raw = headers[i] == null ? "" : headers[i].trim();
                // quitar BOM si existe y normalizar a minúsculas
                raw = raw.replace("\uFEFF", "").toLowerCase();
                headerMap.put(raw, i);
            }

            log.debug("[TxtReaderService] Encabezados detectados (normalizados): {}", headerMap.keySet());

            // 3. Verificar que las columnas clave existan (case-insensitive gracias a lowercasing)
            boolean hasPeriodo = headerMap.containsKey("periodo");
            boolean hasEstu = headerMap.containsKey("estu_consecutivo");

            if (!hasPeriodo || !hasEstu) {
                // Intentar normalizar nombres: quitar espacios, guiones, acentos y underscores
                Map<String,Integer> norm = new HashMap<>();
                for (Map.Entry<String,Integer> e : headerMap.entrySet()) {
                    String k = e.getKey().replace(" ", "").replace("-", "").replace("_", "").replace("í","i").replace("ó","o");
                    norm.put(k, e.getValue());
                }
                if (!hasPeriodo) {
                    if (norm.containsKey("periodo")) { headerMap.put("periodo", norm.get("periodo")); hasPeriodo = true; }
                    else if (norm.containsKey("periodoano")) { headerMap.put("periodo", norm.get("periodoano")); hasPeriodo = true; }
                }
                if (!hasEstu) {
                    if (norm.containsKey("estuconsecutivo")) { headerMap.put("estu_consecutivo", norm.get("estuconsecutivo")); hasEstu = true; }
                    else {
                        // buscar cualquier encabezado que contenga 'consec' o 'consecut'
                        for (String k : norm.keySet()) {
                            if (k.contains("consec") || k.contains("consecut")) {
                                headerMap.put("estu_consecutivo", norm.get(k));
                                hasEstu = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Si aún faltan, asumimos que el archivo puede venir sin header; usamos mapeo por defecto
            if (!hasPeriodo || !hasEstu) {
                log.warn("encabezados 'periodo' y/o 'estu_consecutivo' no encontrados en header. Se usará mapeo por defecto (periodo=col0, estu_consecutivo=col1). Encabezados detectados: {}", headerMap.keySet());
                headerMap.putIfAbsent("periodo", 0);
                headerMap.putIfAbsent("estu_consecutivo", 1);
            }

            String line;
            // 4. Leer el resto de líneas (los datos)
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(java.util.regex.Pattern.quote(delimiter), -1);

                try {
                    ExternalGeneralResult dto = new ExternalGeneralResult();

                    // --- INICIO DEL MAPEO DINÁMICO ---
                    // Usamos el mapa para encontrar la columna, sin importar el orden

                    // periodo (tomamos solo el año si vienen formatos extra)
                    if (headerMap.containsKey("periodo") && isValid(values, headerMap.get("periodo"))) {
                        try {
                            String rawPeriodo = values[headerMap.get("periodo")].trim();
                            if (!rawPeriodo.isEmpty()) {
                                try {
                                    dto.setPeriodo((int) Long.parseLong(rawPeriodo));
                                } catch (NumberFormatException nfe) {
                                    String cleaned = rawPeriodo.replaceAll("\\D+", "");
                                    if (!cleaned.isEmpty()) dto.setPeriodo((int) Long.parseLong(cleaned));
                                }
                            }
                        } catch (Exception ex) {
                            log.warn("no se pudo parsear 'periodo' en línea: {} -> {}", line, ex.getMessage());
                        }
                    }

                    // estu_consecutivo
                    if (headerMap.containsKey("estu_consecutivo") && isValid(values, headerMap.get("estu_consecutivo"))) {
                        dto.setEstConsecutivo(values[headerMap.get("estu_consecutivo")].trim());
                    }

                    // estu_discapacidad
                    if (headerMap.containsKey("estu_discapacidad") && isValid(values, headerMap.get("estu_discapacidad"))) {
                        dto.setEstuDiscapacidad(values[headerMap.get("estu_discapacidad")].trim());
                    }

                    // estu_inst_departamento
                    if (headerMap.containsKey("estu_inst_departamento") && isValid(values, headerMap.get("estu_inst_departamento"))) {
                        // revertir: almacenar tal cual como texto (trim), no como Integer
                        dto.setEstuInstDepartamento(values[headerMap.get("estu_inst_departamento")].trim());
                    }

                    // estu_inst_municipio
                    if (headerMap.containsKey("estu_inst_municipio") && isValid(values, headerMap.get("estu_inst_municipio"))) {
                        // revertir: almacenar tal cual como texto (trim), no como Integer
                        dto.setEstuInstMunicipio(values[headerMap.get("estu_inst_municipio")].trim());
                    }

                    // estu_nucleo_pregrado
                    if (headerMap.containsKey("estu_nucleo_pregrado") && isValid(values, headerMap.get("estu_nucleo_pregrado"))) {
                        dto.setEstuNucleoPregrado(values[headerMap.get("estu_nucleo_pregrado")].trim());
                    }

                    // estu_prgm_academico
                    if (headerMap.containsKey("estu_prgm_academico") && isValid(values, headerMap.get("estu_prgm_academico"))) {
                        dto.setEstuPrgmAcademico(values[headerMap.get("estu_prgm_academico")].trim());
                    }

                    // estu_snies_prgmacademico (antes se parseaba a Integer) -> ahora guardar como Integer para coincidir con BD
                    if (headerMap.containsKey("estu_snies_prgmacademico") && isValid(values, headerMap.get("estu_snies_prgmacademico"))) {
                        String raw = values[headerMap.get("estu_snies_prgmacademico")].trim();
                        dto.setEstuSniesPrgmacademico(parseIntegerSafe(raw));
                    }

                    // inst_cod_institucion -> guardar como Integer para coincidir con BD
                    if (headerMap.containsKey("inst_cod_institucion") && isValid(values, headerMap.get("inst_cod_institucion"))) {
                        String raw = values[headerMap.get("inst_cod_institucion")].trim();
                        dto.setInstCodInstitucion(parseIntegerSafe(raw));
                    }

                    // inst_nombre_institucion
                    if (headerMap.containsKey("inst_nombre_institucion") && isValid(values, headerMap.get("inst_nombre_institucion"))) {
                        dto.setInstNombreInstitucion(values[headerMap.get("inst_nombre_institucion")].trim());
                    }

                    // MÓDULOS y percentiles -> BigDecimal (preservar decimales exactos)
                    if (headerMap.containsKey("mod_competen_ciudada_punt") && isValid(values, headerMap.get("mod_competen_ciudada_punt"))) {
                        dto.setModCompetenCiudadaPunt(parseBigDecimalSafe(values[headerMap.get("mod_competen_ciudada_punt")].trim()));
                    }
                    if (headerMap.containsKey("mod_competen_ciudada_pnbc") && isValid(values, headerMap.get("mod_competen_ciudada_pnbc"))) {
                        dto.setModCompetenCiudadaPnbc(parseBigDecimalSafe(values[headerMap.get("mod_competen_ciudada_pnbc")].trim()));
                    }
                    if (headerMap.containsKey("mod_competen_ciudada_pnal") && isValid(values, headerMap.get("mod_competen_ciudada_pnal"))) {
                        dto.setModCompetenCiudadaPnal(parseBigDecimalSafe(values[headerMap.get("mod_competen_ciudada_pnal")].trim()));
                    }

                    if (headerMap.containsKey("mod_comuni_escrita_punt") && isValid(values, headerMap.get("mod_comuni_escrita_punt"))) {
                        dto.setModComuniEscritaPunt(parseBigDecimalSafe(values[headerMap.get("mod_comuni_escrita_punt")].trim()));
                    }
                    if (headerMap.containsKey("mod_comuni_escrita_pnbc") && isValid(values, headerMap.get("mod_comuni_escrita_pnbc"))) {
                        dto.setModComuniEscritaPnbc(parseBigDecimalSafe(values[headerMap.get("mod_comuni_escrita_pnbc")].trim()));
                    }
                    if (headerMap.containsKey("mod_comuni_escrita_pnal") && isValid(values, headerMap.get("mod_comuni_escrita_pnal"))) {
                        dto.setModComuniEscritaPnal(parseBigDecimalSafe(values[headerMap.get("mod_comuni_escrita_pnal")].trim()));
                    }

                    if (headerMap.containsKey("mod_ingles_punt") && isValid(values, headerMap.get("mod_ingles_punt"))) {
                        dto.setModInglesPunt(parseBigDecimalSafe(values[headerMap.get("mod_ingles_punt")].trim()));
                    }
                    if (headerMap.containsKey("mod_ingles_pnbc") && isValid(values, headerMap.get("mod_ingles_pnbc"))) {
                        dto.setModInglesPnbc(parseBigDecimalSafe(values[headerMap.get("mod_ingles_pnbc")].trim()));
                    }
                    if (headerMap.containsKey("mod_ingles_pnal") && isValid(values, headerMap.get("mod_ingles_pnal"))) {
                        dto.setModInglesPnal(parseBigDecimalSafe(values[headerMap.get("mod_ingles_pnal")].trim()));
                    }

                    if (headerMap.containsKey("mod_lectura_critica_punt") && isValid(values, headerMap.get("mod_lectura_critica_punt"))) {
                        dto.setModLecturaCriticaPunt(parseBigDecimalSafe(values[headerMap.get("mod_lectura_critica_punt")].trim()));
                    }
                    if (headerMap.containsKey("mod_lectura_critica_pnbc") && isValid(values, headerMap.get("mod_lectura_critica_pnbc"))) {
                        dto.setModLecturaCriticaPnbc(parseBigDecimalSafe(values[headerMap.get("mod_lectura_critica_pnbc")].trim()));
                    }
                    if (headerMap.containsKey("mod_lectura_critica_pnal") && isValid(values, headerMap.get("mod_lectura_critica_pnal"))) {
                        dto.setModLecturaCriticaPnal(parseBigDecimalSafe(values[headerMap.get("mod_lectura_critica_pnal")].trim()));
                    }

                    if (headerMap.containsKey("mod_razona_cuantitat_punt") && isValid(values, headerMap.get("mod_razona_cuantitat_punt"))) {
                        dto.setModRazonaCuantitatPunt(parseBigDecimalSafe(values[headerMap.get("mod_razona_cuantitat_punt")].trim()));
                    }
                    if (headerMap.containsKey("mod_razona_cuantitativo_pnbc") && isValid(values, headerMap.get("mod_razona_cuantitativo_pnbc"))) {
                        dto.setModRazonaCuantitativoPnbc(parseBigDecimalSafe(values[headerMap.get("mod_razona_cuantitativo_pnbc")].trim()));
                    }
                    if (headerMap.containsKey("mod_razona_cuantitativo_pnal") && isValid(values, headerMap.get("mod_razona_cuantitativo_pnal"))) {
                        dto.setModRazonaCuantitativoPnal(parseBigDecimalSafe(values[headerMap.get("mod_razona_cuantitativo_pnal")].trim()));
                    }

                    // percentiles y punt_global
                    if (headerMap.containsKey("percentil_global") && isValid(values, headerMap.get("percentil_global"))) {
                        dto.setPercentilGlobal(parseBigDecimalSafe(values[headerMap.get("percentil_global")].trim()));
                    }
                    if (headerMap.containsKey("percentil_nbc") && isValid(values, headerMap.get("percentil_nbc"))) {
                        dto.setPercentilNbc(parseBigDecimalSafe(values[headerMap.get("percentil_nbc")].trim()));
                    }
                    if (headerMap.containsKey("punt_global") && isValid(values, headerMap.get("punt_global"))) {
                        dto.setPuntGlobal(parseBigDecimalSafe(values[headerMap.get("punt_global")].trim()));
                    }

                    // si no tenemos periodo o est_consecutivo válidos, ignoramos la fila
                    if (dto.getPeriodo() == null || dto.getEstConsecutivo() == null) {
                        log.warn("línea ignorada por datos incompletos (periodo/estu_consecutivo): " + line);
                    } else {
                        results.add(dto);
                    }

                } catch (Exception e) {
                    // ¡Importante! Loguea el error de una línea específica y continúa
                    log.error("Error al parsear la línea: {}", line, e);
                }
            }
        } catch (Exception e) {
            log.error("Error al leer el archivo TXT: {}", e.getMessage(), e);
            throw new RuntimeException("Error al leer el archivo TXT: " + e.getMessage(), e);
        }

        return results;
    }

    /**
     * Helper para verificar si un valor es seguro para parsear
     */
    private boolean isValid(String[] values, int index) {
        // Verifica que el índice exista y que el valor no sea nulo ni vacío
        return index < values.length && values[index] != null && !values[index].trim().isEmpty();
    }

    private Integer parseIntegerSafe(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        s = s.replace(",", "").replace("\u00A0", "");
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(s);
                return (int) Math.round(d);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private BigDecimal parseBigDecimalSafe(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        // algunos archivos usan coma como separador de miles o decimales; normalizamos
        s = s.replace(" ", "").replace("\u00A0", "");
        // si hay comas y puntos, asumimos que las comas son separador de miles
        if (s.contains(",") && s.contains(".")) {
            s = s.replace(",", "");
        } else if (s.contains(",") && !s.contains(".")) {
            // si solo tiene coma, convertir coma->dot para decimal
            s = s.replace(',', '.');
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            // intentar extraer número
            String cleaned = s.replaceAll("[^0-9.-]", "");
            if (cleaned.isEmpty()) return null;
            try {
                return new BigDecimal(cleaned);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private String detectDelimiter(String headerLine) {
        if (headerLine.contains(";")) return ";";
        if (headerLine.contains(",")) return ",";
        return DEFAULT_DELIMITER;
    }
}