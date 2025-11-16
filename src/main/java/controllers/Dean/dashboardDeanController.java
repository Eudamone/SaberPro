package controllers.Dean;

import application.SceneManager;
import application.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.*;
import views.FxmlView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class dashboardDeanController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;

    @FXML
    private VBox boxLineChartPromedio;

    @FXML
    private VBox boxBarChartProgram;

    @FXML
    private VBox boxRadarChart;

    @FXML
    private HBox legendBox;

    private RadarChart radarChart;
    private RadarChartLegend legend;

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
        BarChart<Number,String> barChart = ChartCreator.boxChartPromedyProgram();
        boxLineChartPromedio.getChildren().add(chart);
        boxBarChartProgram.getChildren().add(barChart);
        setupRadarChart();
        setupLegend();
        cargarDatosEjemplo();
    }


    @FXML
    void logout(ActionEvent event) throws IOException {
        // Setear el usuario en null para hacer el cierre de sesión
        sessionContext.setCurrentUser(null);
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }

    private void setupRadarChart(){
        radarChart = new RadarChart(500,500);
        radarChart.setMaxValue(300);
        radarChart.setLevels(4);
        radarChart.setShowScaleValues(true);

        // Configurar etiquetas de los ejes
        List<String> labels = Arrays.asList(
                "Lectura crítica",
                "Comunicación\nescrita",
                "Competencias\nciudadanas",
                "Razonamiento\ncuantitativo",
                "Inglés"
        );
        radarChart.setLabels(labels);

        // Hacer que el chart se adapte al contenedor
        radarChart.widthProperty().bind(boxRadarChart.widthProperty());
        radarChart.heightProperty().bind(boxRadarChart.heightProperty());

        // Añadimos al contenedor
        boxRadarChart.getChildren().add(radarChart);
    }

    private void setupLegend() {
        legend = new RadarChartLegend();
        legendBox.getChildren().add(legend);
    }

    private void cargarDatosEjemplo() {
        radarChart.clearSeries();

        // Crear las series de datos usando la clase DataSeries
        DataSeries sistemas = new DataSeries(
                "Ing. de sistemas",
                Arrays.asList(220.0, 280.0, 260.0, 300.0, 200.0),
                Color.rgb(33, 150, 243) // Azul
        );

        DataSeries electronica = new DataSeries(
                "Ing. Electrónica",
                Arrays.asList(210.0, 290.0, 270.0, 220.0, 180.0),
                Color.rgb(156, 39, 176) // Púrpura
        );

        DataSeries ambiental = new DataSeries(
                "Ing. Ambiental",
                Arrays.asList(100.0, 270.0, 180.0, 190.0, 190.0),
                Color.rgb(76, 175, 80) // Verde
        );

        DataSeries procesos = new DataSeries(
                "Ing. de Procesos",
                Arrays.asList(290.0, 185.0, 275.0, 110.0, 170.0),
                Color.rgb(255, 235, 59) // Amarillo
        );

        DataSeries biologia = new DataSeries(
                "Biología",
                Arrays.asList(130.0, 195.0, 290.0, 270.0, 185.0),
                Color.rgb(244, 67, 54) // Rojo
        );

        // Añadir las series al chart
        radarChart.addSeries(sistemas);
        radarChart.addSeries(electronica);
        radarChart.addSeries(ambiental);
        radarChart.addSeries(procesos);
        radarChart.addSeries(biologia);

        // Actualizar la legenda
        legend.updateLegend(radarChart.getSeries());
    }
}
