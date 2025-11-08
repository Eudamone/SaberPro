package services;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
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

    private static final String N8N_URL = "http://localhost:5678/webhook-test/analisis-resultados";

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
}
