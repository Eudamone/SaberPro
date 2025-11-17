package controllers.Dean;

import application.SessionContext;
import dto.InternResultInfo;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import services.CatalogService;
import services.EmailService;
import services.N8NClientService;
import utils.MultiSelectComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.scene.Node;
import utils.Normalized;

@Component
public class consultResultsDeanController {

    private N8NClientService  n8NClientService;
    private final EmailService emailService;
    private final CatalogService catalogService;
    private final SessionContext sessionContext;

    @FXML
    private HBox boxPrueba,boxArea,boxNBC;
    @FXML
    private TableView<InternResultInfo> internResultTable;

    @FXML
    private TableColumn<InternResultInfo, String> nombreColumn;

    @FXML
    private TableColumn<InternResultInfo, String> numberRegisterColumn;

    @FXML
    private TableColumn<InternResultInfo, Integer> periodColumn;

    @FXML
    private TableColumn<InternResultInfo, String> programColumn;

    @FXML
    private TableColumn<InternResultInfo, Integer> puntajeColumn;

    @FXML
    private Pagination pagination;


    consultResultsDeanController(
            N8NClientService n8NClientService,
            EmailService emailService,
            CatalogService catalogService,
            SessionContext sessionContext
    ) {
        this.n8NClientService = n8NClientService;
        this.emailService = emailService;
        this.catalogService = catalogService;
        this.sessionContext = sessionContext;
    }

    @FXML
    void closeSession(ActionEvent event) {

    }

    @FXML
    void handleViewChange(ActionEvent event) {

    }

    @FXML
    public void initialize(){
        setupColumns(); // Se inician columnas
        setupPagination(); // Configura la paginación de consultas
        setupPeriods(); //
        setupAreas();
        setupNBC();
    }

    private void setupColumns(){
        // Configurar columnas
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        numberRegisterColumn.setCellValueFactory(new PropertyValueFactory<>("numeroRegistro"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programa"));
        puntajeColumn.setCellValueFactory(new PropertyValueFactory<>("ptjeGlobal"));
    }

    private void setupPagination(){
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(10);
        Integer total = catalogService.sizeInternResults();
        System.out.println("Total resultados internos: " + total);
        // calcular número de páginas
        int pageCount = (int) Math.ceil((double) total / 15);
        pagination.setPageCount(pageCount);

        // Callback que se usa al cambiar de pagina
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex){
        Page<InternResultInfo> resultPage = catalogService.findInternResults(pageIndex,15);
        ObservableList<InternResultInfo> data = FXCollections.observableArrayList(resultPage.getContent());

        internResultTable.setItems(data);

        // transición de desplazamiento
        FadeTransition fade = new FadeTransition(Duration.millis(250), internResultTable);
        fade.setFromValue(0.7); // empieza casi visible
        fade.setToValue(1.0);   // termina totalmente visible
        fade.play();

        return internResultTable;
    }

    private void setupPeriods(){
        List<Integer> periods = catalogService.getPeriodsResult();
        List<String> periodString = new ArrayList<>();
        for(Integer i : periods){
            periodString.add(String.valueOf(i));
        }
        MultiSelectComboBox multi = new MultiSelectComboBox("Seleccione periodos",periodString);
        boxPrueba.getChildren().addAll(multi);
    }

    private void setupAreas(){
        List<String> areas = catalogService.getAreas();
        MultiSelectComboBox multiProgram =  new MultiSelectComboBox("Seleccione areás",areas);
        boxArea.getChildren().addAll(multiProgram);
    }

    private void setupNBC(){
        Set<String> nbc = catalogService.getNBCDean(sessionContext.getCurrentUser().getId());
        List<String> data = new ArrayList<>(nbc);
        MultiSelectComboBox multiNBC = new MultiSelectComboBox("Seleccione NBC",data);
        boxNBC.getChildren().addAll(multiNBC);
    }

    @FXML
    void filter(MouseEvent event) {
        Node node = boxPrueba.getChildren().get(0);
        MultiSelectComboBox multiPeriod = (MultiSelectComboBox) node;
        System.out.println("Elecciones periodo: " + multiPeriod.getSelectedItems());

    }
}
