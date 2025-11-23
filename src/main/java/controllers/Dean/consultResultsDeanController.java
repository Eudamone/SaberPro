package controllers.Dean;

import application.SessionContext;
import dto.InternResultFilter;
import dto.InternResultInfo;
import dto.InternResultReport;
import dto.ModulePerformance;
import dto.ReportContextStats;
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
    private Label reportExternalAverageLabel;

    @FXML
    private TableView<ModulePerformance> internalModuleTable;

    @FXML
    private TableColumn<ModulePerformance, String> internalModuleNameColumn;

    @FXML
    private TableColumn<ModulePerformance, String> internalModuleAverageColumn;

    @FXML
    private TableColumn<ModulePerformance, String> internalModuleStdDevColumn;

    @FXML
    private TableColumn<ModulePerformance, String> internalModuleMinColumn;

    @FXML
    private TableColumn<ModulePerformance, String> internalModuleMaxColumn;

    @FXML
    private TableColumn<ModulePerformance, String> internalModuleSampleColumn;

    @FXML
    private TableView<ModulePerformance> externalModuleTable;

    @FXML
    private TableColumn<ModulePerformance, String> externalModuleNameColumn;

    @FXML
    private TableColumn<ModulePerformance, String> externalModuleAverageColumn;

    @FXML
    private TableColumn<ModulePerformance, String> externalModuleStdDevColumn;

    @FXML
    private TableColumn<ModulePerformance, String> externalModuleMinColumn;

    @FXML
    private TableColumn<ModulePerformance, String> externalModuleMaxColumn;

    @FXML
    private TableColumn<ModulePerformance, String> externalModuleSampleColumn;

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
        setupReportTables();
    }

    private void setupColumns() {
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        numberRegisterColumn.setCellValueFactory(new PropertyValueFactory<>("numeroRegistro"));
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programa"));
        puntajeColumn.setCellValueFactory(new PropertyValueFactory<>("ptjeGlobal"));
    }

    private void setupModuleTable(TableView<ModulePerformance> table,
                                  TableColumn<ModulePerformance, String> name,
                                  TableColumn<ModulePerformance, String> avg,
                                  TableColumn<ModulePerformance, String> std,
                                  TableColumn<ModulePerformance, String> min,
                                  TableColumn<ModulePerformance, String> max,
                                  TableColumn<ModulePerformance, String> sample) {
        name.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModuleName()));
        avg.setCellValueFactory(cell -> new SimpleStringProperty(formatDecimal(cell.getValue().getStatistics().getAverage())));
        std.setCellValueFactory(cell -> new SimpleStringProperty(formatDecimal(cell.getValue().getStatistics().getStandardDeviation())));
        min.setCellValueFactory(cell -> new SimpleStringProperty(formatInteger(cell.getValue().getStatistics().getMin())));
        max.setCellValueFactory(cell -> new SimpleStringProperty(formatInteger(cell.getValue().getStatistics().getMax())));
        sample.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getStatistics().getSampleSize())));
        table.setItems(FXCollections.observableArrayList());
    }

    private void setupReportTables() {
        setupModuleTable(internalModuleTable,
                internalModuleNameColumn,
                internalModuleAverageColumn,
                internalModuleStdDevColumn,
                internalModuleMinColumn,
                internalModuleMaxColumn,
                internalModuleSampleColumn);
        setupModuleTable(externalModuleTable,
                externalModuleNameColumn,
                externalModuleAverageColumn,
                externalModuleStdDevColumn,
                externalModuleMinColumn,
                externalModuleMaxColumn,
                externalModuleSampleColumn);
    }

    private String formatDecimal(Double value) {
        if (value == null) {
            return "-";
        }
        return String.format("%.2f", value);
    }

    private String formatDecimal(double value) {
        return String.format("%.2f", value);
    }

    private String formatInteger(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private void populateReport(InternResultReport report) {
        ReportContextStats context = report.getContext();
        reportContextLabel.setText(buildContextText(context));
        reportPopulationLabel.setText(String.valueOf(context.getEvaluatedCount()));
        reportAverageLabel.setText(formatDecimal(report.getInternalGlobal().getAverage()));
        reportExternalAverageLabel.setText(formatDecimal(report.getExternalGlobal().getAverage()));
        reportTrendLabel.setText(String.format("%.2f%%", report.getTrendVariation()));
        reportBestModuleLabel.setText(report.getBestModule().map(ModulePerformance::getModuleName).orElse("N/A"));
        reportCriticalModuleLabel.setText(report.getCriticalModule().map(ModulePerformance::getModuleName).orElse("N/A"));

        ObservableList<ModulePerformance> internalData = FXCollections.observableArrayList(report.getInternalModules());
        ObservableList<ModulePerformance> externalData = FXCollections.observableArrayList(report.getExternalModules());
        internalModuleTable.setItems(internalData);
        externalModuleTable.setItems(externalData);
        containerReport.setVisible(true);
    }

    private String buildContextText(ReportContextStats context) {
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


    private List<String> collectSelections(MultiSelectComboBox combo) {
        return combo == null ? new ArrayList<>() : new ArrayList<>(combo.getSelectedItems());
    }
}
