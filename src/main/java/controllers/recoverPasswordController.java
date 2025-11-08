package controllers;

import application.SceneManager;
import application.SessionContext;
import dto.UsuarioInfoDTO;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.EmailService;
import services.UsuarioService;
import utils.Alerts;
import utils.NavigationHelper;

import java.time.LocalDateTime;
import java.util.UUID;


@Component
public class recoverPasswordController {

    private final SceneManager sceneManager;
    private final UsuarioService usuarioService;
    private final EmailService emailService;
    private final SessionContext sessionContext;

    @Lazy
    public recoverPasswordController(SceneManager sceneManager, UsuarioService usuarioService, EmailService emailService,SessionContext sessionContext) {
        this.sceneManager = sceneManager;
        this.usuarioService = usuarioService;
        this.emailService = emailService;
        this.sessionContext = sessionContext;
    }

    @FXML
    private VBox errorAlert;

    @FXML
    private VBox confirmAlert;


    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImageLogin;

    @FXML
    private Rectangle overlayRect;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField tfToken;

    @FXML
    private Label lbError;

    @FXML
    private VBox loadingOverlay;


    @FXML
    public void initialize(){
        backgroundImageLogin.setPreserveRatio(false);
        backgroundImageLogin.setSmooth(true);
        backgroundImageLogin.setCache(true);

        backgroundImageLogin.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageLogin.fitHeightProperty().bind(rootPane.heightProperty());

        overlayRect.widthProperty().bind(rootPane.widthProperty());
        overlayRect.heightProperty().bind(rootPane.heightProperty());
    }


    @FXML
    void sendEmailRecoverPassword(ActionEvent event) {
        String email = txtEmail.getText();

        if(email.isEmpty()){
            lbError.setText("El campo para el correo esta vacío");
            errorAlert.setVisible(true);
            return;
        } else if (!usuarioService.existEmailForUser(email)) {
            lbError.setText("El correo ingresado no existe");
            errorAlert.setVisible(true);
            return;
        }

        UsuarioInfoDTO user = usuarioService.findByEmail(email);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        usuarioService.saveResetToken(user.getId(),token,expiry);

        Task<Boolean> sendEmailTask = new Task<>() {
            @Override
            protected Boolean call() {
                try{
                    emailService.sendPasswordResetEmail(email,token);
                    return true;
                }catch(Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
        };

        sendEmailTask.setOnRunning(e -> {loadingOverlay.setVisible(true);});

        sendEmailTask.setOnSucceeded(e -> {
            loadingOverlay.setVisible(false);
            if(!sendEmailTask.getValue()){
                lbError.setText("Error al enviar el correo");
                errorAlert.setVisible(true);
            }else{
                // Definimos el usuario temporal en el contexto para diferenciar el logueo
                sessionContext.setUserTemp(user);

                // Se muestra el contenedor para ingresar el token
                confirmAlert.setVisible(true);
            }
        });

        sendEmailTask.setOnFailed(e -> {
            loadingOverlay.setVisible(false);
            lbError.setText("Error al enviar el correo");
            errorAlert.setVisible(true);
        });

        new  Thread(sendEmailTask).start();

        //emailService.sendPasswordResetEmail(email,token);
    }

    @FXML
    void cancelRecoverPassword(ActionEvent event) throws Exception {
        // Esto regresa a la vista del login
        NavigationHelper.handleViewChange(event,sceneManager);
    }

    @FXML
    void closeAlert(ActionEvent event) {
        Node node =(Node) event.getSource();
        node.getParent().setVisible(false);
    }

    @FXML
    void continueChangePassword(ActionEvent event) throws Exception {
        String token = this.tfToken.getText();

        if(token.isEmpty()){
            Alerts.showError("El campo esta vacío","Error");
            return;
        }

        // isTokenValid comprueba si el token existe y no está caducado
        if(usuarioService.isTokenValid(token)){
            // Esto cambia a la vista de cambio de contraseña
            NavigationHelper.handleViewChange(event,sceneManager);
        }else{
            Alerts.showError("El token ingresado no es correcto, ya venció o no existe","Error");
        }
    }
}
