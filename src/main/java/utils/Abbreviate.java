package utils;

public class Abbreviate {
    public static String abbreviateProgram(String text){
        if(text == null || text.trim().isEmpty()){
            return "";
        }

        String[] parts = text.split(" ");

        if(parts.length > 1){
            switch(parts[0]){
                case "Ingeniería","Ingenieria" -> {
                    parts[0] = "Ing.";
                    return String.join(" ", parts);
                }
                case "Licenciatura" ->{
                    parts[0] = "Lic.";
                    return String.join(" ", parts);
                }
            }
        }
        return text;
    }
}
