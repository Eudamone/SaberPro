package utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Normalized {

    // Palabras que no aportan a la abreviatura
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "UNIVERSIDAD", "COLEGIO", "DE", "DEL", "LA", "LOS", "LAS",
            "SUPERIORES", "ESTUDIOS", "ADMINISTRACION"
    ));

    // Método para quitar tildes y poner en mayúsculas
    public static String limpiarYMayusculas(String input) {
        if (input == null) return null;
        // Normaliza el texto a forma NFD (descompone caracteres acentuados)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        // Elimina los caracteres diacríticos (acentos, tildes)
        String sinTildes = normalized.replaceAll("\\p{M}", "");
        // Convierte a mayúsculas
        return sinTildes.toUpperCase();
    }

    public static String primerMayuscula(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Manejo de casos nulos o vacíos
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Genera una abreviatura para nombres largos de universidades.
     * Ejemplos:
     * UNIVERSIDAD DE LOS ANDES-BOGOTÁ D.C. -> UA-BOGOTÁ
     * UNIVERSIDAD EIA-MEDELLIN -> EIA-MEDELLIN
     * UNIVERSIDAD NACIONAL DE COLOMBIA-BOGOTÁ D.C. -> UNC-BOGOTÁ
     * COLEGIO DE ESTUDIOS SUPERIORES DE ADMINISTRACION-CESA-BOGOTÁ D.C. -> CESA-BOGOTÁ
     */
    public static String abreviarUniversidad(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "";
        }

        // Separar institución y ciudad
        String[] partes = nombre.split("-");
        String institucion = partes[0].trim();
        String ciudad = partes.length > 1 ? partes[1].trim() : "";

        // Procesar palabras
        String[] palabras = institucion.split("\\s+");
        StringBuilder abreviatura = new StringBuilder();

        for (String palabra : palabras) {
            String upper = palabra.toUpperCase();

            if (!STOP_WORDS.contains(upper)) {
                // Si parece sigla (ej: EIA, CESA), conservar completa
                if (upper.length() <= 4 && upper.equals(palabra)) {
                    abreviatura.append(upper);
                } else {
                    // Tomar inicial
                    abreviatura.append(upper.charAt(0));
                }
            }
        }

        // Agregar ciudad (solo primera palabra)
        if (!ciudad.isEmpty()) {
            abreviatura.append("-").append(ciudad.split("\\s+")[0].toUpperCase());
        }

        return abreviatura.toString();
    }

}
