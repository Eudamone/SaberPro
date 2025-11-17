package utils;

import java.text.Normalizer;

public class Normalized {
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
}
