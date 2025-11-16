package controllers;

import application.SceneManager;
import dto.UsuarioSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.AuthService;
import services.UsuarioService;
import views.FxmlView;

import java.io.IOException;


@Component
public class loginController {

    private final SceneManager sceneManager;
    private final AuthService authService;
    private final UsuarioService usuarioService;

    @Lazy
    public loginController(SceneManager sceneManager, AuthService authService, UsuarioService usuarioService) {
        this.sceneManager = sceneManager;
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @FXML
    private ImageView backgroundImageLogin;

    @FXML
    private StackPane rootPane;

    @FXML
    private Rectangle overlayRect;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUser;

    @FXML
    void handleLoginButton(MouseEvent event) throws IOException {
        sceneManager.switchToNextScene(FxmlView.RECOVER_PASSWORD);
    }

    @FXML
    void accessIn(ActionEvent event) throws IOException {
        String username = (txtUser != null) ? txtUser.getText() : null;
        String password = (txtPassword != null) ? txtPassword.getText() : null;

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Por favor, ingrese su usuario y contraseña.");
            return;
        }

        // Autenticación con BD (bcrypt)
        UsuarioSession session = authService.login(username, password);
        if (session == null) {
            showError("Credenciales no válidas.");
            return;
        } else if (usuarioService.hasUserNew(session.id())){
            sceneManager.switchToNextScene(FxmlView.CHANGE_PASSWORD); // Si el usuario es nuevo cambia a la vista de cambiar contraseña
            return;
        }

        // Política de acceso (demo): si es Decano → dashboard del decano; si no, dashboard genérico
        try {
            switch (session.getRol()){
                case Decano -> {
                    sceneManager.switchToNextScene(FxmlView.DASHBOARD_DEAN);
                }
                case Estudiante -> {
                    sceneManager.switchToNextScene(FxmlView.RESULTS_STUDENT);
                }
                case Administrador -> {
                    sceneManager.switchToNextScene(FxmlView.LOAD_RESULTS_ADMIN);
                }
                default -> {
                    showError("No existe una vista para el usuario ingresado");
                }
            }
        } catch (Exception e) {
            showError("No se puede abrir la siguiente vista.");
            e.printStackTrace();
        }
    }

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

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null);
        a.setTitle("Login");
        a.setContentText(msg);
        a.showAndWait();
    }
}
