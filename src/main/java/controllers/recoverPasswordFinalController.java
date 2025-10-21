package controllers;

import application.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.springframework.stereotype.Component;


@Component
public class recoverPasswordFinalController {

    @FXML
    private StackPane rootPane;

    @FXML
    private ImageView backgroundImageLogin;

    @FXML
    private Rectangle overlayRect;

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
    void changeScene(ActionEvent event) {

    }
}
