package controllers;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.AuthService;
import views.FxmlView;

import java.io.IOException;


@Component
public class loginController {

    private final SceneManager sceneManager;
    private final AuthService authService;

    @Lazy
    public loginController(SceneManager sceneManager, AuthService authService) {
        this.sceneManager = sceneManager;
        this.authService = authService;
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
    void accessIn(ActionEvent event) {
        String username = (txtUser != null) ? txtUser.getText() : null;
        String password = (txtPassword != null) ? txtPassword.getText() : null;

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Por favor, ingrese su usuario y contraseña.");
            return;
        }

        // Autenticación con BD (bcrypt)
        Usuario user = authService.login(username, password);
        if (user == null) {
            showError("Credenciales no válidas.");
            return;
        }

        // Política de acceso (demo): si es Decano → dashboard del decano; si no, dashboard genérico
        try {
            if ("Decano".equalsIgnoreCase(user.getRol())) {
                sceneManager.switchToNextScene(FxmlView.DASHBOARD_DEAN);
            } else {
                sceneManager.switchToNextScene(FxmlView.DASHBOARD_USER);
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
