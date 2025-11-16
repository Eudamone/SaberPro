package services;

public class DataFileProcessor {

    /*
    public Stream<ResultadoSaberPro> streamAndLoad(String filePath){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            String headerLine = reader.readLine();
            Map<String,Integer> columnMapping = parserHeader(headerLine);
        } catch (IOException e) {
            System.err.println("Error"+ e.getMessage() +" ,al leer el archivo en la ruta: " + filePath);
        }
    }

    private Map<String,Integer> parserHeader(String headerLine){
        //Lógica para identificar las columnas
    }

    private ResultadoSaberPro ConvertLineToResult(String line,Map<String,Integer> columnMapping){
        // Dividir línea por delimitador
        String[] fields =  line.split(";");

        ResultadoSaberPro register = new ResultadoSaberPro();

        register.setPeriodo();
    }

     */
}
