package controllers.Dean;

import application.SessionContext;
import dto.InternResultFilter;
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
import javafx.scene.layout.VBox;
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

@Component
public class consultResultsDeanController {

    private static final int PAGE_SIZE = 15;

    private final N8NClientService n8NClientService;
    private final EmailService emailService;
    private final CatalogService catalogService;
    private final SessionContext sessionContext;

    @FXML
    private HBox boxPrueba, boxArea, boxNBC;

    @FXML
    private VBox containerReport;

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

    private MultiSelectComboBox multiPeriodCombo;
    private MultiSelectComboBox multiAreaCombo;
    private MultiSelectComboBox multiNBCCombo;

    private InternResultFilter currentFilter = new InternResultFilter();

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
    public void initialize() {
        setupColumns();
        setupPagination();
        setupPeriods();
        setupAreas();
        setupNBC();
    }

    private void setupColumns() {
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        numberRegisterColumn.setCellValueFactory(new PropertyValueFactory<>("numeroRegistro"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programa"));
        puntajeColumn.setCellValueFactory(new PropertyValueFactory<>("ptjeGlobal"));
    }

    private void setupPagination() {
        pagination.setMaxPageIndicatorCount(10);
        pagination.setPageFactory(this::createPage);
        updatePagination();
        pagination.setCurrentPageIndex(0);
    }

    private void updatePagination() {
        long total = catalogService.countInternResults(currentFilter);
        int pageCount = total == 0 ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        pagination.setPageCount(pageCount);
    }

    private Node createPage(int pageIndex) {
        Page<InternResultInfo> resultPage = fetchPage(pageIndex);
        ObservableList<InternResultInfo> data = FXCollections.observableArrayList(resultPage.getContent());

        internResultTable.setItems(data);

        FadeTransition fade = new FadeTransition(Duration.millis(250), internResultTable);
        fade.setFromValue(0.7);
        fade.setToValue(1.0);
        fade.play();

        return internResultTable;
    }

    private Page<InternResultInfo> fetchPage(int pageIndex) {
        return catalogService.findInternResults(pageIndex, PAGE_SIZE, currentFilter);
    }

    private void setupPeriods() {
        List<Integer> periods = catalogService.getPeriodsResult();
        List<String> periodString = new ArrayList<>();
        for (Integer period : periods) {
            periodString.add(String.valueOf(period));
        }
        multiPeriodCombo = new MultiSelectComboBox("Seleccione periodos", periodString);
        boxPrueba.getChildren().addAll(multiPeriodCombo);
    }

    private void setupAreas() {
        List<String> areas = catalogService.getAreas();
        multiAreaCombo = new MultiSelectComboBox("Seleccione areás", areas);
        boxArea.getChildren().addAll(multiAreaCombo);
    }

    private void setupNBC() {
        Set<String> nbc = catalogService.getNBCDean(sessionContext.getCurrentUser().getId());
        List<String> data = new ArrayList<>(nbc);
        multiNBCCombo = new MultiSelectComboBox("Seleccione NBC", data);
        boxNBC.getChildren().addAll(multiNBCCombo);
    }

    @FXML
    void filter(MouseEvent event) {
        List<String> periods = collectSelections(multiPeriodCombo);
        List<String> areas = collectSelections(multiAreaCombo);
        List<String> nbc = collectSelections(multiNBCCombo);

        currentFilter = new InternResultFilter(parsePeriods(periods), areas, nbc);
        updatePagination();
        pagination.setCurrentPageIndex(0);
    }

    private List<Integer> parsePeriods(List<String> selections) {
        List<Integer> parsed = new ArrayList<>();
        for (String selection : selections) {
            try {
                parsed.add(Integer.valueOf(selection));
            } catch (NumberFormatException ignored) {
            }
        }
        return parsed;
    }

    private List<String> collectSelections(MultiSelectComboBox combo) {
        return combo == null ? new ArrayList<>() : new ArrayList<>(combo.getSelectedItems());
    }
}
