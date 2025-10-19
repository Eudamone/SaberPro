package controllers;

import application.SceneManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import views.FxmlView;

import java.io.IOException;

@Component
public class dashboardDeanController {

    private final SceneManager sceneManager;

    @Lazy
    public dashboardDeanController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    void changeToCreateUser(ActionEvent event) throws IOException {
        sceneManager.switchToNextScene(FxmlView.CREATE_USER);
    }

    @FXML
    public void initialize() {

    }
}
