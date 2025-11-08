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
import service.TxtReaderService; // ¡Importamos el lector de TXT!

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LoadMassiveUsersController {

    private static final Logger log = LoggerFactory.getLogger(LoadMassiveUsersController.class);

    // --- INICIO DE LA SOLUCIÓN (Error 2) ---
    // Inyectamos el TxtReaderService directamente
    @Autowired
    private TxtReaderService txtReaderService;
    // --- FIN DE LA SOLUCIÓN (Error 2) ---

    @FXML
    private ComboBox<String> cbTipoArchivo;

    @FXML
    private Button btnCargar;

    @FXML
    private Label lblFileName;

    // --- INICIO DE LA SOLUCIÓN (Error 1) ---
    // Declaramos las variables que faltaban
    private File selectedFile;
    private Stage stage;
    // --- FIN DE LA SOLUCIÓN (Error 1) ---


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

        // Esta fue la corrección anterior, que sigue siendo válida
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        this.stage = (Stage) source.getScene().getWindow(); // Ahora 'stage' sí existe

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

        try (InputStream is = new FileInputStream(selectedFile)) {

            // --- INICIO DE LA SOLUCIÓN (Error 2) ---
            // Ya no llamamos a processFile, sino al TxtReaderService

            if (fileType.equals("EXTERNO_GENERAL") || fileType.equals("EXTERNO_ESPECIFICO")) {

                // Llamamos directamente al servicio que SÍ existe
                List<ExternalGeneralResult> results = txtReaderService.readTxtFile(is); // recibimos la lista de resultados

                // Por ahora, solo confirmamos la lectura
                // El siguiente paso sería guardar 'results' en la base de datos
                showAlert(Alert.AlertType.INFORMATION, "Lectura Exitosa", "Se leyeron " + results.size() + " filas del archivo TXT.");

            } else if (fileType.equals("INTERNO")) {
                // Aún no tienes un ExcelReaderService en tu paquete 'service'
                showAlert(Alert.AlertType.ERROR, "No implementado", "El lector de archivos INTERNOS (Excel) aún no está implementado.");

            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Tipo de archivo no reconocido.");
            }
            // --- FIN DE LA SOLUCIÓN (Error 2) ---

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