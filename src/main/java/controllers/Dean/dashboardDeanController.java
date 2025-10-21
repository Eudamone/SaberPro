package controllers.Dean;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.NavigationHelper;

@Component
public class dashboardDeanController {

    private final SceneManager sceneManager;

    @Lazy
    public dashboardDeanController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event,sceneManager);
    }

    @FXML
    public void initialize() {

    }
}
