package controllers;

import application.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.springframework.stereotype.Component;
import utils.Paths;

@Component
public class recoverPasswordController {

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImageLogin;

    @FXML
    private Rectangle overlayRect;

    @FXML
    private TextField txtEmail;


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
        // Implementar logica de envios de correos



    }
}
