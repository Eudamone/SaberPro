package controllers;

import application.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.Paths;
import views.FxmlView;

import java.io.IOException;


@Component
public class loginController {

    private final SceneManager sceneManager;

    @Lazy
    public loginController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
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
