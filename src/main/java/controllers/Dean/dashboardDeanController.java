package controllers.Dean;

import application.SceneManager;
import application.SessionContext;
import dto.DatosRadarChart;
import dto.ModuloPromedio;
import dto.PromedioAnioDTO;
import dto.PromedioProgram;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.CatalogService;
import utils.*;
import views.FxmlView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class dashboardDeanController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;
    private final CatalogService catalogService;

    @FXML
    private StackPane rootPane;

    @FXML
    private VBox boxLineChartPromedio;

    @FXML
    private VBox boxBarChartProgram;

    @FXML
    private VBox boxRadarChart;

    @FXML
    private HBox legendBox;

    @FXML
    private Label percentilExtern;

    @FXML
    private Label percentilIntern;

    @FXML
    private Label promedyExtern;

    @FXML
    private Label promedyIntern;


    private RadarChart radarChart;
    private RadarChartLegend legend;

    @Lazy
    public dashboardDeanController(SceneManager sceneManager,SessionContext sessionContext,CatalogService catalogService) {
        this.sceneManager = sceneManager;
        this.sessionContext = sessionContext;
        this.catalogService = catalogService;
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event,sceneManager,rootPane);
    }

    @FXML
    public void initialize() {
        setupLineChartPromedio(); // Se configura la gráfica de promedio por periodo
        setupBarChartProgram(); // Se configura la gráfica de promedio por programa
        setupRadarChart();
        setupLegend();
        cargarDatosRadarChart();
        setupIndicatorsPromedy();
    }

    private void setupLineChartPromedio() {
        List<Integer> p = catalogService.getPeriodsResult();
        List<String> periodos = p.stream()
                .map(String::valueOf)
                .toList();
        LineChart<String,Number> chart = ChartCreator.createLineChart(periodos);
        cargarDatosPromedio(chart);
        boxLineChartPromedio.getChildren().add(chart);
    }

    private void setupBarChartProgram() {
        List<PromedioProgram> programsDean = catalogService.getPromedioProgramasFacultad(
                catalogService.getCodeFaculty(sessionContext.getCurrentUser().getId())
        );
        List<String> programs = programsDean.stream().map(PromedioProgram::getPrograma).toList();
        BarChart<Number,String> barChart = ChartCreator.boxChartPromedyProgram(programs);
        cargarDatosProgramaPromedio(barChart,programsDean);
        boxBarChartProgram.getChildren().add(barChart);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        // Setear el usuario en null para hacer el cierre de sesión
        sessionContext.logout();
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }

    private void setupRadarChart(){
        radarChart = new RadarChart(500,500);
        radarChart.setMaxValue(300);
        radarChart.setLevels(4);
        radarChart.setShowScaleValues(true);

        // Configurar etiquetas de los ejes
        List<String> labels = Arrays.asList(
                "Competencias\nciudadanas",
                "Comunicación\nescrita",
                "Inglés",
                "Lectura crítica",
                "Razonamiento\ncuantitativo"
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

    private void cargarDatosPromedio(LineChart<String,Number> lineChart){
        List<PromedioAnioDTO> promedios = catalogService.getPromediosAnio();
        XYChart.Series<String,Number> series = new XYChart.Series<>();
        series.setName("Promedio de año");
        for (PromedioAnioDTO promedio : promedios) {
            series.getData().add(new XYChart.Data<>(promedio.getAnioString(),promedio.getPromedio()));
        }
        lineChart.getData().add(series);
        for(XYChart.Data<String,Number> data: series.getData()){
            Tooltip tooltip = new Tooltip(
                    "Promedio: " + data.getYValue().toString()
            );
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().setStyle("-fx-background-color: transparent;");
        }
    }

    private void cargarDatosProgramaPromedio(BarChart<Number,String> barChart,List<PromedioProgram> programsPromedy){
        XYChart.Series<Number,String> series = new XYChart.Series<>();
        series.setName("Promedio Programa");
        for(PromedioProgram promedioProgram : programsPromedy){
            series.getData().add(new XYChart.Data<>(promedioProgram.getPromedio(),promedioProgram.getPrograma()));
        }
        barChart.getData().add(series);
        for(XYChart.Data<Number,String> data: series.getData()){
            Tooltip tooltip = new Tooltip(
                "Promedio: " + data.getXValue().toString()
            );
            Tooltip.install(data.getNode(), tooltip);
        }
    }

    private void cargarDatosRadarChart() {
        radarChart.clearSeries();
        List<String> programsDean = catalogService.getProgramsDean(sessionContext.getCurrentUser().getId());
        List<DatosRadarChart> datos = programsDean.stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .map(programa -> {
                    List<ModuloPromedio> modulos = catalogService.getModuloPromedio(programa);

                    // Retornar un valor nulo si el programa no tiene datos de modulos
                    if(modulos == null || modulos.isEmpty()){
                        return null;
                    }

                    DatosRadarChart datosRadarChart = new DatosRadarChart();
                    datosRadarChart.setPrograma(Abbreviate.abbreviateProgram(programa));
                    datosRadarChart.setModulos(modulos);
                    return datosRadarChart;
                })
                .filter(Objects::nonNull)
                .toList();
        // Lectura Crítica - Comunicación escrita - Competencias Ciudadanas - Razonamiento cuantitativo - Inglés
        int colorIndex = 0;
        for(DatosRadarChart datosRadarChart: datos){
            // Se toman los valores de los promedios de cada modulo
            List<Double> valoresPromedio = datosRadarChart.getModulos().stream()
                    .map(ModuloPromedio::getPromedio)
                    .toList();

            // Se le asigna un color a la serie
            Color colorSerie = RadarChart.COLORS_SERIES.get(colorIndex % RadarChart.COLORS_SERIES.size());

            DataSeries serie = new DataSeries(
                    datosRadarChart.getPrograma(),
                    valoresPromedio,
                    colorSerie
            );

            radarChart.addSeries(serie);

            colorIndex++;
        }
        // Actualizar la legenda
        legend.updateLegend(radarChart.getSeries());
    }

    private void setupIndicatorsPromedy(){
        promedyIntern.setText(catalogService.getPromedyGeneralFacultadDean(
                sessionContext.getCurrentUser().getId()
        ).toString());
        promedyExtern.setText(catalogService.getPromedyExtern().toString());
        percentilIntern.setText(catalogService.getPercentilGeneralFacultadDean(
                sessionContext.getCurrentUser().getId()
        ).toString());
        percentilExtern.setText(catalogService.getPercentilGeneral().toString());
    }
}
