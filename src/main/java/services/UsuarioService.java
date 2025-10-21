package services;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import model.Decano;
import model.Estudiante;
import model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.DecanoRepository;
import repository.EstudianteRepository;
import repository.UsuarioRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UsuarioService {

    private UsuarioRepository usuarioRepository;

    private EmailService emailService;

    private final EstudianteRepository estudianteRepository;
    private final DecanoRepository  decanoRepository;

    private PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarioRepository, EmailService emailService, PasswordEncoder encoder,EstudianteRepository estudianteRepository,DecanoRepository decanoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.encoder = encoder;
        this.estudianteRepository =  estudianteRepository;
        this.decanoRepository = decanoRepository;
    }

    private static final String RUTA_INSTRUCTIVO_PDF = "src/main/resources/docs/FO-DOC-112 GUIA 6. LAB MEDIOS NO GUIADOS.pdf"; // ¡Ajusta esta ruta real!
    private static final String NOMBRE_ADJUNTO = "Instructivo_Acceso_SaberPro.pdf";

    @Transactional
    public Usuario createAndNotify(
            String username,
            String email,
            String name,
            String typeDocument,
            String numberDocument,
            String Rol
    ) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setNombre(name);
        usuario.setDocument(Usuario.typeDocument.valueOf(typeDocument));
        usuario.setNumIdentification(numberDocument);
        usuario.setRol(Usuario.rolType.valueOf(Rol));

        String passTemporal = generatePasswordAleatory();
        usuario.setPass(encoder.encode(passTemporal));

        Usuario userSaved = usuarioRepository.save(usuario);

        try{
            String htmlBody = generateHtmlCredentials(userSaved.getUsername(), passTemporal);

            emailService.sendEmailNewUser(
                    userSaved.getEmail(),
                    "Bienvenido a Saber Pro: Sus Credenciales de Acceso",
                    htmlBody,
                    RUTA_INSTRUCTIVO_PDF,
                    NOMBRE_ADJUNTO
            );
        }catch (MessagingException e){
            System.err.println("Error al enviar correo al usuario " + userSaved.getUsername() + ": " + e.getMessage());
        }

        return userSaved;
    }

    private String generateHtmlCredentials(String username,String password){
        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Credenciales de Acceso a SaberPro</title>
                </head>
                <body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: Arial, sans-serif;">
                
                    <table border="0" cellpadding="0" cellspacing="0" width="100%" style="table-layout: fixed;">
                        <tr>
                            <td align="center" style="padding: 20px 0 30px 0;">
                
                                <table border="0" cellpadding="0" cellspacing="0" width="600" style="border-collapse: collapse; border: 1px solid #dddddd; background-color: #ffffff; box-shadow: 0 4px 8px rgba(0,0,0,0.05);">
                
                                    <tr>
                                        <td align="center" style="padding: 30px 0 20px 0; background-color: #FC0610;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px;">Bienvenido a Saber Pro</h1>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td style="padding: 40px 30px 40px 30px;">
                
                                            <h2 style="color: #333333; margin: 0 0 20px 0; font-size: 24px;">
                                                ¡Su cuenta ha sido creada con éxito!
                                            </h2>
                
                                            <p style="color: #555555; margin: 0 0 30px 0; font-size: 16px; line-height: 24px;">
                                                A continuación, encontrará sus credenciales de acceso al sistema <b>Saber Pro</b>. Por favor, guárdelas de forma segura.
                                            </p>
                
                                            <table border="0" cellpadding="10" cellspacing="0" width="100%" style="background-color: #f2cac7; border: 1px solid #d9e7f7; border-radius: 5px; margin-bottom: 30px;">
                                                <tr>
                                                    <td width="30%" style="color: #FC0610; font-weight: bold; font-size: 16px;">Nombre de Usuario:</td>
                                                    <td width="70%" style="color: #333333; font-weight: bold; font-size: 16px;">[NOMBRE_USUARIO_DINAMICO]</td>
                                                </tr>
                                                <tr>
                                                    <td width="30%" style="color: #FC0610; font-weight: bold; font-size: 16px;">Contraseña Temporal:</td>
                                                    <td width="70%" style="color: #333333; font-weight: bold; font-size: 16px;">[CONTRASEÑA_DINAMICA]</td>
                                                </tr>
                                            </table>
                
                                            <p style="color: #555555; margin: 0 0 20px 0; font-size: 16px; line-height: 24px;">
                                                Para conocer el proceso de ingreso y las primeras configuraciones, hemos adjuntado un <b>Instructivo de Acceso</b> en formato PDF. Revíselo antes de iniciar sesión.
                                            </p>
                
                                            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                                <tr>
                                                    <td align="center" style="padding: 20px 0 20px 0;">
                                                        <table border="0" cellpadding="0" cellspacing="0">
                                                            <tr>
                                                                <td align="center" bgcolor="#FC0610" style="border-radius: 5px;">
                                                                    <a href="[URL_DE_ACCESO_AQUI]" target="_blank" style="font-size: 18px; font-weight: bold; font-family: Arial, sans-serif; color: #ffffff; text-decoration: none; padding: 12px 25px; border: 1px solid #FC0610; display: inline-block; border-radius: 5px;">
                                                                        Iniciar Sesión Ahora
                                                                    </a>
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                
                
                                            <p style="color: #333333; margin: 30px 0 0 0; font-size: 16px; font-weight: bold;">
                                                ¡Gracias por unirse a Saber Pro!
                                            </p>
                                            <p style="color: #333333; margin: 5px 0 0 0; font-size: 16px;">
                                                Soporte Técnico
                                            </p>
                                        </td>
                                    </tr>
                
                                    <tr>
                                        <td align="center" style="padding: 20px 30px 20px 30px; background-color: #e6e6e6; color: #777777; font-size: 12px;">
                                            <p style="margin: 0;">
                                                Este correo fue enviado de forma automatica.
                                            </p>
                                            <p style="margin: 5px 0 0 0;">
                                                &copy; 2025 SaberPro. Todos los derechos reservados.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """;
        return htmlContent
                .replace("[NOMBRE_USUARIO_DINAMICO]",username)
                .replace("[CONTRASEÑA_DINAMICA]",password);
    }

    private String generatePasswordAleatory() {

        // 1. Conjuntos de caracteres
        final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String DIGITS = "0123456789";
        final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>/?";

        // Conjunto total de caracteres disponibles
        final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;

        // Longitud deseada de la contraseña
        final int LENGTH = 8;

        // Usar SecureRandom para una generación criptográficamente segura
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder(LENGTH);

        // 2. Asegurar que la contraseña contenga al menos un carácter de cada tipo
        // Esto es crucial para cumplir con políticas de seguridad comunes
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // 3. Llenar el resto de la contraseña con caracteres aleatorios del conjunto total
        for (int i = 4; i < LENGTH; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        // 4. Mezclar (shuffle) los caracteres para que los obligatorios no estén siempre al principio
        // Esto mejora la seguridad.
        return shuffleString(password.toString());
    }

    // Función para mezclar los caracteres de una cadena
    private String shuffleString(String input) {
        java.util.List<Character> characters = new java.util.ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        java.util.Collections.shuffle(characters);

        StringBuilder output = new StringBuilder(input.length());
        for (char c : characters) {
            output.append(c);
        }
        return output.toString();
    }

    @Transactional
    public List<Estudiante> bulkCreateStudents(List<Estudiante> estudiantes) throws MessagingException {
        List<Estudiante> savedEstudiantes = new ArrayList<>();

        for (Estudiante estudiante : estudiantes) {
            // 1. Generar Contraseña y Hash
            String passTemporal = generatePasswordAleatory();
            Usuario usuarioTemp = estudiante.getUsuario();

            usuarioTemp.setPass(encoder.encode(passTemporal));

            // 2. Guardar Usuario base
            Usuario userSaved = usuarioRepository.save(usuarioTemp);

            // 3. Guardar Entidad Estudiante especializada (usando el ID generado)
            estudiante.setId(userSaved.getId()); // ¡Crucial para la relación OneToOne!
            estudiante.setUsuario(userSaved);
            Estudiante saved = estudianteRepository.save(estudiante);

            // 4. Enviar Notificación
            String htmlBody = generateHtmlCredentials(userSaved.getUsername(), passTemporal);
            emailService.sendEmailNewUser(
                    userSaved.getEmail(),
                    "Bienvenido a Saber Pro: Sus Credenciales de Acceso",
                    htmlBody,
                    RUTA_INSTRUCTIVO_PDF,
                    NOMBRE_ADJUNTO
            );

            savedEstudiantes.add(saved);
        }
        return savedEstudiantes;
    }

    public List<Usuario> findAllUsers() {
        return usuarioRepository.findAll();
    }

    public Optional<Decano> findDecanoById(Long userId) {
        return decanoRepository.findById(userId);
    }

    @Transactional
    public void updateUserAndSpecializedEntity(Usuario updatedUser, String codeTeaching, String typeTeaching) {
        // a) Guardar Usuario base
        Usuario existingUser = usuarioRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualizar."));

        // Actualizar solo los campos modificables del usuario base
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setNombre(updatedUser.getNombre());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setNumIdentification(updatedUser.getNumIdentification());
        existingUser.setDocument(updatedUser.getDocument());
        // NO actualizar el Rol aquí, ya que cambiaría la estructura de la BD

        usuarioRepository.save(existingUser);

        // b) Actualizar entidades especializadas
        if (existingUser.getRol() == Usuario.rolType.Decano.getTipo() || existingUser.getRol() == Usuario.rolType.Docente.getTipo()) {
            Decano decano = decanoRepository.findById(existingUser.getId())
                    .orElseGet(Decano::new); // Si no existe, crear uno (caso raro, pero seguro)

            decano.setId(existingUser.getId());
            decano.setUsuario(existingUser);
            decano.setCodeTeacher(codeTeaching);
            decano.setTipoDocente(Decano.tipoDocente.valueOf(typeTeaching.toUpperCase()));

            decanoRepository.save(decano);
        }
        // Se puede añadir lógica para Estudiante aquí
        // else if (existingUser.getRol() == Usuario.rolType.Estudiante) { ... }
    }

    //  Eliminar Usuario (Transaccional)
    @Transactional
    public void deleteUser(Long userId) {
        // La eliminación debe manejar la entidad especializada primero debido a las restricciones de FK.

        Optional<Usuario> userOpt = usuarioRepository.findById(userId);
        if (userOpt.isEmpty()) return;
        Usuario user = userOpt.get();

        // Eliminar entidades especializadas manualmente (sin cascade)
        if (user.getRol() == Usuario.rolType.Decano.getTipo() || user.getRol() == Usuario.rolType.Docente.getTipo()) {
            decanoRepository.deleteById(userId);
        } else if (user.getRol() == Usuario.rolType.Estudiante.getTipo()) {
            estudianteRepository.deleteById(userId); // Asumiendo EstudianteRepository existe
        }

        // Eliminar el usuario base
        usuarioRepository.delete(user);
    }
}
