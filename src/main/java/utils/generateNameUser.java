package utils;

import javafx.scene.control.TextField;
import services.UsuarioService;

import java.text.Normalizer;

public class generateNameUser {
    public static void generateUsername(TextField userName, String name, String document, UsuarioService usuarioService){
        if(name.isEmpty() || document.isEmpty()){
            userName.setText("");
            return;
        }

        name = name.trim();
        document = document.trim();

        // Normalizar y quitar diacríticos (tildes, ñ combinada, etc.)
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFD).
                replaceAll("\\p{M}","");

        // Mantener solo letras y espacios (propiedad Unicode para letras)
        normalized = normalized.replaceAll("[^\\p{L}\\s]","").toLowerCase().trim();

        // Colapsar espacios múltiples
        normalized = normalized.replaceAll("\\s{2,}", " ");

        // Partir tokens
        String[] parts = normalized.split("\\s+");
        if(parts.length == 0){
            userName.setText("");
            return;
        }

        // Primer nombre
        String firstName = parts[0];

        // Se toma el último apellido
        String lastName = parts.length > 1 ? parts[parts.length-1] : "";

        // Tomar los últimos 3 dígitos del documento
        String docSuffix = document.length() > 3 ? document.substring(document.length() - 3) : document;

        // Se genera una base
        String baseUsername;
        if(!lastName.isEmpty()){
            baseUsername = firstName + "." + lastName + docSuffix;
        }else{
            baseUsername = firstName + docSuffix;
        }

        // Limpiar por si queda algún carácter raro (solo alfanumérico y punto y guion bajo)
        baseUsername = baseUsername.replaceAll("[^\\p{Alnum}._-]", "");

        // Se crea un nombre de usuario único
        String uniqueUserName = uniqueUserName(baseUsername,usuarioService);

        userName.setText(uniqueUserName);
    }

    private static String uniqueUserName(String userName,UsuarioService usuarioService){
        String candidate = userName;
        int counter = 1;

        while(usuarioService.getUsuarioRepository().existsByUsername(candidate)){
            candidate = userName + counter;
            counter++;
        }
        return candidate;
    }
}
