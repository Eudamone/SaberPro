package controllers.Dean;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.NavigationHelper;

@Component
public class loadMassiveUsersDeanController {

    private final SceneManager sceneManager;

    @Lazy
    loadMassiveUsersDeanController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    void handleViewChange(ActionEvent actionEvent)throws Exception {
        NavigationHelper.handleViewChange(actionEvent, sceneManager);
    }
}
