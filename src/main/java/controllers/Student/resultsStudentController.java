package controllers.Student;

import application.SceneManager;
import application.SessionContext;
import dto.MejorModulo;
import dto.ModuloPromedio;
import dto.Ranking;
import dto.UniversidadPromedio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.CatalogService;
import utils.*;
import views.FxmlView;

import java.io.IOException;
import java.util.*;

@Component
public class resultsStudentController {

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;
    private final CatalogService catalogService;

    @FXML
    private VBox barChartComparationIES; // Contenedor de gráfico de barras comparativo con otras IES

    @FXML
    private Label lbCantidadEstUniversidad; // Cantidad de estudiantes con resultados de la universidad (De [NUM] estudiantes)

    @FXML
    private Label lbCantidadPrograma; // Cantidad de estudiantes con resultados del programa (De [NUM] estudiantes)

    @FXML
    private Label lbMejorModulo; // Mejor módulo del estudiante

    @FXML
    private Label lbPorcentajePercentilGlobal; // Porcentaje de percentil global ([NUM]%)

    @FXML
    private Label lbPuestoPrograma; // Puesto del estudiante en el programa (#numero)

    @FXML
    private Label lbPuestoUniversidad; // Puesto del estudiante en la universidad (#numero)

    @FXML
    private Label lbPuntajeGlobal; // Puntaje global del estudiante ([NUM]/300)

    @FXML
    private Label lbPuntajeModulo; // Puntaje estudiante en el módulo ([NUM] puntos)

    @FXML
    private ProgressBar pgBarPercentilGlobal; //Barra de progresión con el porcentaje de percentil (0-100)

    @FXML
    private ProgressBar pgBarPuntajeGlobal; // Barra de progresión con el puntaje del estudiante global (0-300)

    @FXML
    private VBox radarChartCompetencies; // Contenedor de gráfico de radar para los módulos

    @FXML
    private HBox boxLegends; // Contenedor para las legendas del radar chart

    @FXML
    private Label lbPuestoDepartamento;

    @FXML
    private Label lbPuestoNacional;

    @FXML
    private Label lbCantidadDepartamento;

    @FXML
    private Label lbCantidadNacional;

    private RadarChart radarChart;
    private RadarChartLegend radarChartLegend;

    @Lazy
    resultsStudentController(SceneManager sceneManager, SessionContext sessionContext,CatalogService catalogService) {
        this.sceneManager = sceneManager;
        this.sessionContext = sessionContext;
        this.catalogService = catalogService;
    }

    @FXML
    void changeScene(ActionEvent event) {

    }

    @FXML
    void downloadInformPDF(MouseEvent event) {

    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        sessionContext.logout();
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }

    @FXML
    public void initialize(){
        setupIndicators();
        setupChartCompetencies();
        setupLegend();
        cargarDatosRadarCompetencies();
        setupBarChartPromedyResult();
    }

    private void setupIndicators(){
        // Puesto del estudiante en la universidad en el año de su resultado
        lbPuestoUniversidad.setText(
                "#" + catalogService.getPuestoUniversidad(sessionContext.getCurrentUser().getNumIdentification())
        );
        lbCantidadEstUniversidad.setText(
                lbCantidadEstUniversidad.getText().replace(
                        "[NUM]",catalogService.getSizeInternalResultAnio(sessionContext.getCurrentUser().getNumIdentification()).toString()
                )
        );

        // Puesto del estudiante en el programa en el año de su resultado
        lbPuestoPrograma.setText(
                "#" +  catalogService.getPuestoPrograma(sessionContext.getCurrentUser().getNumIdentification())
        );
        lbCantidadPrograma.setText(
                lbCantidadPrograma.getText().replace(
                        "[NUM]",catalogService.getSizeInternalResultAnioProgram(sessionContext.getCurrentUser().getNumIdentification()).toString()
                )
        );

        // Mejor módulo del estudiante
        MejorModulo mejorModulo = catalogService.getMejorModulo(sessionContext.getCurrentUser().getNumIdentification());
        lbMejorModulo.setText(Normalized.primerMayuscula(mejorModulo.getNombre()));
        lbPuntajeModulo.setText(
                lbPuntajeModulo.getText().replace(
                        "[NUM]",mejorModulo.getPuntaje().toString()
                )
        );

        // Barra de progreso con percentil nacional
        Integer percentilNac = catalogService.getPercentilNacionalStudent(sessionContext.getCurrentUser().getNumIdentification());
        pgBarPercentilGlobal.setProgress(percentilNac / 100.0);
        lbPorcentajePercentilGlobal.setText(
                lbPorcentajePercentilGlobal.getText().replace(
                        "[NUM]",percentilNac.toString()
                )
        );

        //Barra de progreso con puntaje global
        Integer puntajeGlobal = catalogService.getPuntajeGlobalStudent(sessionContext.getCurrentUser().getNumIdentification());
        pgBarPuntajeGlobal.setProgress(puntajeGlobal / 300.0);
        lbPuntajeGlobal.setText(
                lbPuntajeGlobal.getText().replace(
                        "[NUM]",puntajeGlobal.toString()
                )
        );
        // Posición nacional
        Ranking puestoNac = catalogService.getPuestoNacionalByAnio(sessionContext.getCurrentUser().getNumIdentification());
        lbPuestoNacional.setText(
                "#" + puestoNac.getPosicion().toString()
        );
        lbCantidadNacional.setText(
                lbCantidadNacional.getText().replace(
                        "[NUM]",puestoNac.getTotalEstudiantes().toString()
                )
        );
        // Puntaje departamental
        Ranking puestoDep = catalogService.getPuestoDepartamentalByAnio(sessionContext.getCurrentUser().getNumIdentification());
        lbPuestoDepartamento.setText(
                "#" + puestoDep.getPosicion().toString()
        );
        lbCantidadDepartamento.setText(
                lbCantidadDepartamento.getText().replace(
                        "[NUM]",puestoDep.getTotalEstudiantes().toString()
                )
        );
    }

    private void setupChartCompetencies(){
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
        radarChart.widthProperty().bind(radarChartCompetencies.widthProperty());
        radarChart.heightProperty().bind(radarChartCompetencies.heightProperty());

        // Añadimos al contenedor
        radarChartCompetencies.getChildren().add(radarChart);
    }

    private void setupLegend() {
        radarChartLegend = new RadarChartLegend();
        boxLegends.getChildren().add(radarChartLegend);
    }

    private void cargarDatosRadarCompetencies(){
        radarChart.clearSeries();

        // Hay 3 series: Tu resultado, Programa, Institución
        // Modulos estudiante
        List<ModuloPromedio> modulosStudent = catalogService.getPromedioModulosStudent(sessionContext.getCurrentUser().getNumIdentification());
        List<Double> valoresPromedioStudent = modulosStudent.stream().map(ModuloPromedio::getPromedio).toList();
        DataSeries serieStudent = new DataSeries(
                "Tu resultado",
                valoresPromedioStudent,
                RadarChart.COLORS_SERIES.get(0)
        );

        // Modulos programa - General
        String programa = catalogService.getProgramStudent(sessionContext.getCurrentUser().getNumIdentification());
        Integer periodo = catalogService.getPeriodoResult(sessionContext.getCurrentUser().getNumIdentification());
        List<ModuloPromedio> modulosPrograma = catalogService.getModuloPromedioByAnio(programa,periodo);
        List<Double> valoresPromedioPrograma = modulosPrograma.stream().map(ModuloPromedio::getPromedio).toList();
        DataSeries seriePrograma = new DataSeries(
                "Programa",
                valoresPromedioPrograma,
                RadarChart.COLORS_SERIES.get(1)
        );

        // Modulos Universidad
        List<ModuloPromedio> modulosUniversidad = catalogService.getModuloPromedioGeneralByAnio(periodo);
        List<Double> valoresPromedioUni = modulosUniversidad.stream().map(ModuloPromedio::getPromedio).toList();
        DataSeries serieUniversidad = new DataSeries(
                "Institución",
                valoresPromedioUni,
                RadarChart.COLORS_SERIES.get(2)
        );

        radarChart.addSeries(serieStudent);
        radarChart.addSeries(seriePrograma);
        radarChart.addSeries(serieUniversidad);
        radarChartLegend.updateLegend(radarChart.getSeries());
    }

    private void setupBarChartPromedyResult(){
        Integer ptjeGlobalEstudiante = catalogService.getPuntajeGlobalStudent(
                sessionContext.getCurrentUser().getNumIdentification()
        );
        List<UniversidadPromedio> ptjePromedioUniversidades = catalogService.getMejoresUniversidadPorPeriodo(
                sessionContext.getCurrentUser().getNumIdentification()
        );

        List<String> listaBarChart = new ArrayList<>(
                ptjePromedioUniversidades.stream()
                        .map(u -> Normalized.abreviarUniversidad(u.getNombre()))
                        .toList()
        );
        listaBarChart.add("Tu resultado");

        BarChart<Number,String> barChart = ChartCreator.boxChartPromedyProgram(listaBarChart);

        Map<String,String> nombreCompletoMap = new HashMap<>();

        // Cargar datos en un solo paso
        XYChart.Series<Number,String> series = new XYChart.Series<>();
        series.setName("Promedio Puntaje Global");

        for (UniversidadPromedio promedio : ptjePromedioUniversidades) {
            String abreviado = Normalized.abreviarUniversidad(promedio.getNombre());
            XYChart.Data<Number,String> data = new XYChart.Data<>(promedio.getPromedio(), abreviado);

            series.getData().add(data);
            nombreCompletoMap.put(abreviado, promedio.getNombre());
        }

        // Agregar resultado del estudiante
        XYChart.Data<Number,String> tuResultado = new XYChart.Data<>(ptjeGlobalEstudiante, "Tu resultado");
        series.getData().add(tuResultado);
        nombreCompletoMap.put("Tu resultado", "Tu resultado");

        barChart.getData().add(series);

        // Instalar tooltips con nombre completo
        for (XYChart.Data<Number,String> data : series.getData()) {
            String nombreCompleto = nombreCompletoMap.get(data.getYValue());
            Tooltip tooltip = new Tooltip(
                    nombreCompleto + "\nPromedio: " + data.getXValue()
            );
            Tooltip.install(data.getNode(), tooltip);
        }

        barChartComparationIES.getChildren().add(barChart);
    }

}