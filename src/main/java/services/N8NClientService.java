package services;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

@Service
public class N8NClientService {

    private static final String N8N_URL = "http://100.103.51.4:5678/webhook-test/analisis-resultados";

    public void enviarDatosPrueba(){
        try{
            JSONObject json = new JSONObject();
            json.put("programa","Ingenieria Software I");
            json.put("anio",2024);
            json.put("usuario","adrian");
            json.put("timestamp",Instant.now().getEpochSecond());
            String payload = json.toString();

            System.out.println("Enviando POST a n8n: " + N8N_URL);
            System.out.println("Datos: " + payload);

            URL url = new URL(N8N_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);

            // Leer respuesta
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("Respuesta de N8N: " + response);
            }

            conn.disconnect();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void enviarReporteYRecibirArchivo(JSONObject object, Stage stage) {
        HttpURLConnection conn = null;
        try {
            String payload = object.toString();

            System.out.println("Enviando POST a n8n: " + N8N_URL);
            System.out.println("Datos: " + payload);

            URL url = new URL(N8N_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Enviar JSON
            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);

            if (responseCode == 200) {
                // Leer el PDF desde la respuesta
                InputStream inputStream = conn.getInputStream();

                // Abrir FileChooser para que el usuario seleccione dónde guardar el PDF
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Guardar reporte PDF");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF", "*.pdf")
                );
                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                    System.out.println("PDF guardado en: " + file.getAbsolutePath());
                } else {
                    System.out.println("El usuario canceló la descarga del PDF.");
                }

                inputStream.close();
            } else {
                System.out.println("Error al generar el PDF. Código: " + responseCode);
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        StringBuilder error = new StringBuilder();
                        int ch;
                        while ((ch = errorStream.read()) != -1) {
                            error.append((char) ch);
                        }
                        System.out.println("Error: " + error);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Descarga el PDF desde n8n y devuelve los bytes crudos.
     */
    public byte[] descargarPDF(JSONObject payload) throws IOException {
        String N8N_URL = "http://100.103.51.4:5678/webhook/analisis-resultados";
        URL url = new URL(N8N_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/pdf"); // PDF esperado
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Error al descargar PDF, código HTTP: " + responseCode);
        }

        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        } finally {
            conn.disconnect();
        }
    }
}
