package controllers.Dean;

import application.SessionContext;
import dto.InternResultFilter;
import dto.InternResultInfo;
import dto.InternResultReport;
import dto.ModulePerformance;
import dto.ScoreStatistics;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
import java.util.stream.Collectors;

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
    private HBox boxSemestre;

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
    private TableColumn<InternResultInfo, Integer> semesterColumn;

    @FXML
    private TableColumn<InternResultInfo, String> programColumn;

    @FXML
    private TableColumn<InternResultInfo, Integer> puntajeColumn;

    @FXML
    private Pagination pagination;

    @FXML
    private Label reportContextLabel;

    @FXML
    private Label reportPopulationLabel;

    @FXML
    private Label reportAverageLabel;

    @FXML
    private Label reportTrendLabel;

    @FXML
    private Label reportBestModuleLabel;

    @FXML
    private Label reportCriticalModuleLabel;

    @FXML
    private TableView<ModulePerformance> moduleReportTable;

    @FXML
    private TableColumn<ModulePerformance, String> moduleNameColumn;

    @FXML
    private TableColumn<ModulePerformance, String> moduleAverageColumn;

    @FXML
    private TableColumn<ModulePerformance, String> moduleStdDevColumn;

    @FXML
    private TableColumn<ModulePerformance, String> moduleMinColumn;

    @FXML
    private TableColumn<ModulePerformance, String> moduleMaxColumn;

    @FXML
    private TableColumn<ModulePerformance, String> moduleSampleColumn;

    private MultiSelectComboBox multiPeriodCombo;
    private MultiSelectComboBox multiAreaCombo;
    private MultiSelectComboBox multiNBCCombo;
    private MultiSelectComboBox multiSemesterCombo;

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
        setupSemesters();
        setupAreas();
        setupNBC();
        setupReportTable();
    }

    private void setupColumns() {
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        numberRegisterColumn.setCellValueFactory(new PropertyValueFactory<>("numeroRegistro"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programa"));
        puntajeColumn.setCellValueFactory(new PropertyValueFactory<>("ptjeGlobal"));
    }

    private void setupReportTable() {
        moduleNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModuleName()));
        moduleAverageColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDecimal(cell.getValue().getStatistics().getAverage())));
        moduleStdDevColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatDecimal(cell.getValue().getStatistics().getStandardDeviation())));
        moduleMinColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatInteger(cell.getValue().getStatistics().getMin())));
        moduleMaxColumn.setCellValueFactory(cell -> new SimpleStringProperty(formatInteger(cell.getValue().getStatistics().getMax())));
        moduleSampleColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getStatistics().getSampleSize())));
        moduleReportTable.setItems(FXCollections.observableArrayList());
    }

    private String formatDecimal(Double value) {
        if (value == null) {
            return "-";
        }
        return String.format("%.2f", value);
    }

    private String formatInteger(Integer value) {
        return value == null ? "-" : String.valueOf(value);
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
        Page<InternResultInfo> resultPage = fetchValidPage(pageIndex);
        adjustPageCount(resultPage);
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

    private Page<InternResultInfo> fetchValidPage(int pageIndex) {
        Page<InternResultInfo> page = fetchPage(pageIndex);
        if (needsPageClamp(page, pageIndex)) {
            int lastIndex = Math.max(0, page.getTotalPages() - 1);
            if (lastIndex != pageIndex) {
                pagination.setCurrentPageIndex(lastIndex);
                page = fetchPage(lastIndex);
            }
        }
        return page;
    }

    private boolean needsPageClamp(Page<InternResultInfo> page, int requestedIndex) {
        return page.getContent().isEmpty()
                && page.getTotalElements() > 0
                && page.getTotalPages() > 0
                && (requestedIndex >= page.getTotalPages());
    }

    private void adjustPageCount(Page<InternResultInfo> page) {
        int totalPages = page.getTotalPages() == 0 ? 1 : page.getTotalPages();
        if (pagination.getPageCount() != totalPages) {
            pagination.setPageCount(totalPages);
        }
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

    private void setupSemesters() {
        List<Integer> semesters = catalogService.getSemesters();
        List<String> semesterString = new ArrayList<>();
        for (Integer semester : semesters) {
            semesterString.add(String.valueOf(semester));
        }
        multiSemesterCombo = new MultiSelectComboBox("Seleccione semestres", semesterString);
        boxSemestre.getChildren().addAll(multiSemesterCombo);
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
        currentFilter = buildFilterFromSelections();
        updatePagination();
        pagination.setCurrentPageIndex(0);
        createPage(0);
    }

    @FXML
    void generateReport(ActionEvent event) {
        InternResultReport report = catalogService.generateInternReport(currentFilter);
        if (report == null) {
            return;
        }
        populateReport(report);
        containerReport.setVisible(true);
    }

    @FXML
    void hideReport(ActionEvent event) {
        containerReport.setVisible(false);
    }

    private InternResultFilter buildFilterFromSelections() {
        List<String> periods = collectSelections(multiPeriodCombo);
        List<String> areas = collectSelections(multiAreaCombo);
        List<String> nbc = collectSelections(multiNBCCombo);
        List<String> semesters = collectSelections(multiSemesterCombo);

        return new InternResultFilter(
                parsePeriods(periods),
                areas,
                nbc,
                parseSemesters(semesters)
        );
    }

    private void populateReport(InternResultReport report) {
        reportContextLabel.setText(buildContextText(report.getContext()));
        reportPopulationLabel.setText(String.valueOf(report.getContext().getEvaluatedCount()));
        reportAverageLabel.setText(formatDecimal(report.getGlobalStatistics().getAverage()));
        reportTrendLabel.setText(String.format("%.2f%%", report.getTrendVariation()));
        reportBestModuleLabel.setText(report.getBestModule().map(ModulePerformance::getModuleName).orElse("N/A"));
        reportCriticalModuleLabel.setText(report.getCriticalModule().map(ModulePerformance::getModuleName).orElse("N/A"));
        ObservableList<ModulePerformance> moduleData = FXCollections.observableArrayList(report.getModulePerformances());
        moduleReportTable.setItems(moduleData);
        containerReport.setVisible(true);
    }

    private String buildContextText(dto.ReportContext context) {
        List<String> parts = new ArrayList<>();
        if (!context.getPeriods().isEmpty()) {
            parts.add("Periodos: " + joinList(context.getPeriods()));
        }
        if (!context.getSemesters().isEmpty()) {
            parts.add("Semestres: " + joinList(context.getSemesters()));
        }
        if (!context.getAreas().isEmpty()) {
            parts.add("Áreas: " + joinList(context.getAreas()));
        }
        if (!context.getNbc().isEmpty()) {
            parts.add("NBC: " + joinList(context.getNbc()));
        }
        return parts.isEmpty() ? "Sin filtros" : String.join(" | ", parts);
    }

    private String joinList(List<?> values) {
        return values.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
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

    private List<Integer> parseSemesters(List<String> selections) {
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
