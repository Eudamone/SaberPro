package controllers.Student;

import application.SceneManager;
import application.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import views.FxmlView;

import java.io.IOException;

@Component
public class resultsStudentController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;

    @Lazy
    resultsStudentController(SceneManager sceneManager, SessionContext sessionContext) {
        this.sceneManager = sceneManager;
        this.sessionContext = sessionContext;
    }

    @FXML
    void changeScene(ActionEvent event) {

    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        sessionContext.setCurrentUser(null);
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }
}
