package controllers.Dean;

import application.SceneManager;
import application.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.NavigationHelper;
import views.FxmlView;

import java.io.IOException;

@Component
public class dashboardDeanController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;

    @Lazy
    public dashboardDeanController(SceneManager sceneManager,SessionContext sessionContext) {
        this.sceneManager = sceneManager;
        this.sessionContext = sessionContext;
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event,sceneManager);
    }

    @FXML
    public void initialize() {

    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        // Setear el usuario en null para hacer el cierre de sesión
        sessionContext.setCurrentUser(null);
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }
}
