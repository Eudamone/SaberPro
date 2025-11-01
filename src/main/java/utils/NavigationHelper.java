package utils;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import views.FxmlView;

public class NavigationHelper {
    public static void handleViewChange(ActionEvent event, SceneManager sceneManager) throws Exception {
        Object ud = ((Node) event.getSource()).getUserData();
        if (ud == null) return;
        String key = ud.toString();
        FxmlView view = FxmlView.valueOf(key);
        sceneManager.switchToNextScene(view);
    }
}

