package services;

import dto.UsuarioInfoDTO;
import factories.UsuarioFactoryRegistry;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import model.Estudiante;
import model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UsuarioRepository;

import java.security.SecureRandom;
import java.util.*;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final UsuarioFactoryRegistry factoryRegistry;

    public UsuarioService(UsuarioRepository usuarioRepository, EmailService emailService, PasswordEncoder encoder,UsuarioFactoryRegistry factoryRegistry) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.encoder = encoder;
        this.factoryRegistry = factoryRegistry;
    }

    public UsuarioRepository getUsuarioRepository() {
        return usuarioRepository;
    }

    public PasswordEncoder getEncoder() {
        return encoder;
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
            String Rol,
            //Parámetros para usuarios específicos
            Map<String,Object> datos
    ) {
        // Lógica para crear el usuario base
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setNombre(name);
        usuario.setDocument(Usuario.typeDocument.valueOf(typeDocument));
        usuario.setNumIdentification(numberDocument);
        usuario.setRol(Usuario.rolType.valueOf(Rol));
        // Se le asigna una contraseña temporal
        String passTemporal = generatePasswordAleatory();
        usuario.setPass(encoder.encode(passTemporal));
        // Se guarda al usuario
        Usuario userSaved = usuarioRepository.save(usuario);
        // Creamos el usuario especializado según el rol
        factoryRegistry.createUserSpecificFactory(userSaved,datos);

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

    public List<Usuario> findAllUsuarios() {
        return usuarioRepository.findAllUsuarios();
    }

    public List<Estudiante> bulkCreateStudents(List<Estudiante> estudiantes) {
        return estudiantes;
    }

    @Transactional
    public void updateUser(Usuario usuario,String nuevoRol,Map<String,Object> datos) {
        Usuario.rolType rolActual = usuario.getRol();
        Usuario.rolType rolNuevo = Usuario.rolType.fromTipo(nuevoRol);

        // Si el rol cambia hay que eliminar el anterior y crear el nuevo
        if(rolActual != rolNuevo){
            //Se elimina la entidad especializada anterior
            factoryRegistry.deleteUserSpecificFactory(usuario,rolActual.getTipo());
            usuario.setRol(rolNuevo);

            // Se crea la nueva entidad especializada
            factoryRegistry.createUserSpecificFactory(usuario,datos);
        }else{
            // Solo se actualiza la entidad especializada actual
            factoryRegistry.updateUserSpecificFactory(usuario,datos);
        }

        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario findUserForEdit(Long id){
        return usuarioRepository.findByIdForEdit(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: "+id));
    }

    public List<UsuarioInfoDTO> findAllUsersTable(){
        return usuarioRepository.findUsuarioInfo();
    }

    @Transactional
    public void deleteUser(Usuario usuario) {
        if(usuario == null){
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        // Se guarda el rol antes de eliminar
        String rolAnterior =  usuario.getRol().getTipo();

        // Eliminamos la entidad especializada
        factoryRegistry.deleteUserSpecificFactory(usuario,rolAnterior);

        // Eliminamos el usuario de la base de datos
        usuarioRepository.delete(usuario);
    }
}
