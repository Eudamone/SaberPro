package controllers;

import application.SceneManager;
import application.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.UsuarioService;
import utils.Alerts;
import utils.NavigationHelper;
import views.FxmlView;


@Component
public class changePasswordController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;
    private final UsuarioService usuarioService;

    @Lazy
    changePasswordController(SceneManager sceneManager, SessionContext sessionContext, UsuarioService usuarioService) {
        this.sceneManager = sceneManager;
        this.sessionContext = sessionContext;
        this.usuarioService = usuarioService;
    }

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImageLogin;

    @FXML
    private Rectangle overlayRect;

    @FXML
    private PasswordField tfPassword;

    @FXML
    private PasswordField tfRepeatPassword;

    @FXML
    void changePassword(ActionEvent event) throws Exception {
        String password = tfPassword.getText();
        String repeatPassword = tfRepeatPassword.getText();

        if(password.isEmpty() || repeatPassword.isEmpty()) {
            Alerts.showError("Los campos no pueden estar vacíos","Error");
            return;
        }
        if(!tfPassword.getText().equals(tfRepeatPassword.getText())) {
            Alerts.showError("Las contraseñas no coinciden","Error");
            return;
        }

        if(sessionContext.getUserTemp() != null){ // Para usuarios no logueados que quieren restablecer la contraseña
            usuarioService.updatePassword(sessionContext.getUserTemp().getId(),password);
            //Se borra el token del restablecer contraseña y adicionalmente se borran todos los tokens ya expirados
            usuarioService.deleteResetToken(sessionContext.getUserTemp().getId());
            sessionContext.setUserTemp(null);
            sceneManager.switchToNextScene(FxmlView.LOGIN);
        } else if (sessionContext.getCurrentUser() != null) { // Para usuarios logueados por primera vez
            usuarioService.updatePassword(sessionContext.getCurrentUser().getId(),password);
            usuarioService.deleteNewUser(sessionContext.getCurrentUser().getId()); // Eliminar el registro en la tabla nuevousuario
            System.out.println("Rol usuario: "+sessionContext.getCurrentUser().getRol());
            NavigationHelper.changeSceneByRol(sessionContext.getCurrentUser().getRol(),sceneManager);
        } else{
            // Error al tratar de cambiar la contraseña
            System.err.println("Error de cambio de password : userTemp ni userCurrent con valor");
            sceneManager.switchToNextScene(FxmlView.LOGIN);
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



}
