package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ExternalGeneralResult; // Asegúrate de que este import esté
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.FileProcessingService;
import utils.LocalMultipartFile;

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoadMassiveUsersController {

    private static final Logger log = LoggerFactory.getLogger(LoadMassiveUsersController.class);

    // --- INICIO DE LA SOLUCIÓN (Error 2) ---
    // Inyectamos el TxtReaderService directamente
    @Autowired
    private FileProcessingService fileProcessingService;
    // --- FIN DE LA SOLUCIÓN (Error 2) ---

    @FXML
    private ComboBox<String> cbTipoArchivo;

    @FXML
    private Button btnCargar;

    @FXML
    private Label lblFileName;


    private File selectedFile;
    private Stage stage;



    @FXML
    public void initialize() {
        cbTipoArchivo.getItems().addAll("INTERNO", "EXTERNO_GENERAL", "EXTERNO_ESPECIFICO");
    }

    /**
     * Maneja el evento de seleccionar archivo
     */
    @FXML
    private void handleSelectFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo de Resultados");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos Soportados", "*.txt", "*.xlsx"),
                new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt")
        );


        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        this.stage = (Stage) source.getScene().getWindow();

        // Esta línea ahora funciona
        this.selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            lblFileName.setText(selectedFile.getName());
        } else {
            lblFileName.setText("Ningún archivo seleccionado.");
        }
    }

    /**
     * Maneja el evento de Cargar el archivo
     */
    @FXML
    private void handleLoadFile(ActionEvent event) {
        String fileType = cbTipoArchivo.getValue();

        if (selectedFile == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, seleccione un archivo.");
            return;
        }
        if (fileType == null || fileType.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Por favor, seleccione el tipo de archivo.");
            return;
        }

        try {
            // envolver el File en un MultipartFile local para usar FileProcessingService
            String name = selectedFile.getName().toLowerCase();
            String contentType = "application/octet-stream";
            if (name.endsWith(".txt")) contentType = "text/plain";
            else if (name.endsWith(".xlsx")) contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            LocalMultipartFile localFile = new LocalMultipartFile(selectedFile, contentType);

            // Llamar al servicio de procesamiento de archivos
            if (fileType.equals("EXTERNO_GENERAL")) {
                List<ExternalGeneralResult> saved = fileProcessingService.parseAndSaveGeneral(localFile);
                showAlert(Alert.AlertType.INFORMATION, "Carga Exitosa", "Se guardaron " + saved.size() + " filas en Resultado Externo (general). ");
            } else if (fileType.equals("EXTERNO_ESPECIFICO")) {
                // Para específicos pasamos null como periodo por ahora (FileProcessingService no lo requiere estrictamente)
                List<?> saved = fileProcessingService.parseAndSaveSpecifics(localFile, null);
                showAlert(Alert.AlertType.INFORMATION, "Carga Exitosa", "Se guardaron " + saved.size() + " filas en Resultado Externo (específico). ");
            } else if (fileType.equals("INTERNO")) {
                showAlert(Alert.AlertType.ERROR, "No implementado", "El lector de archivos INTERNOS (Excel) aún no está implementado.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Tipo de archivo no reconocido.");
            }

        } catch (Exception e) {
            log.error("Error cargando archivo {}: {}", selectedFile != null ? selectedFile.getAbsolutePath() : "-", e.getMessage(), e);
            showAlert(Alert.AlertType.ERROR, "Error en la Carga", e.getMessage());
        }
    }

    // Método auxiliar para mostrar alertas
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}