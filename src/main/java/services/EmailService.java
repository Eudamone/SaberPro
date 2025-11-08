package services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import repository.UsuarioRepository;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailExecutor")
    public void sendEmailNewUserAsync(String to, String subject, String htmlBody,String rutaAdjunto,String nombreAdjunto) {
        try{
            sendEmailNewUser(
                    to,
                    subject,
                    htmlBody,
                    rutaAdjunto,
                    nombreAdjunto
            );
        }catch (MessagingException e){
            System.err.println("Error al enviar correo asincrónicamente a " + to + ": " + e.getMessage());
        }
    }

    /**
     * Envía un correo electrónico con contenido HTML y un archivo adjunto
     * @param subject Asunto del correo
     * @param htmlBody Contenido del cuerpo del correo en formato HTML
     * @param attachmentPath Ruta absoluta o relativa del archivo a adjuntar
     * @param attachmentName Nombre que tendrá el archivo adjunto en el correo
     * @throws MessagingException
     */
    public void sendEmailNewUser(
            String toEmail,
            String subject,
            String htmlBody,
            String attachmentPath,
            String attachmentName
    ) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setFrom(fromEmail);

        helper.setText(htmlBody, true);

        try{
            File file = new File(attachmentPath);
            if (file.exists() && !file.isDirectory()) {
                // Usa FileSystemResource para leer el archivo desde el disco
                FileSystemResource fileResource = new FileSystemResource(file);
                // Adjuntar el recurso con el nombre deseado
                helper.addAttachment(attachmentName, fileResource);
            } else {
                // Lanza una excepción si el archivo no existe, para registrar el error
                throw new MessagingException("El archivo instructivo no fue encontrado en la ruta: " + attachmentPath);
            }
        }catch(Exception e){
            // Re-lanzar como MessagingException para que la capa de servicio (UsuarioService) lo capture
            throw new MessagingException("Error al adjuntar el archivo: " + e.getMessage(), e);
        }

        // 6. Enviar el mensaje
        mailSender.send(message);
        System.out.println("Correo con credenciales y adjunto enviado a: " + toEmail);
    }

    public void sendPasswordResetEmail(String email, String token) {
        try{
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("token", token);
            String payload = json.toString();

            URL url = new URL("http://localhost:5678/webhook-test/recover-password");

            System.out.println("Enviando POST a N8N: " + url);
            System.out.println("Datos: "+payload);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Recover-Password", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Código de respuesta: " + responseCode);
            if(responseCode != 200){
                throw new MessagingException("Error al enviar el correo: " + responseCode);
            }

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
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
