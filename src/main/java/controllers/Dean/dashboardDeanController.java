package controllers.Dean;

import application.SceneManager;
import application.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.ChartCreator;
import utils.NavigationHelper;
import views.FxmlView;

import java.awt.*;
import java.io.IOException;

@Component
public class dashboardDeanController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;

    @FXML
    private VBox boxLineChartPromedio;

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
        LineChart<String,Number> chart = ChartCreator.createLineChart();
        boxLineChartPromedio.getChildren().add(chart);
    }


    @FXML
    void logout(ActionEvent event) throws IOException {
        // Setear el usuario en null para hacer el cierre de sesión
        sessionContext.setCurrentUser(null);
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }
}
