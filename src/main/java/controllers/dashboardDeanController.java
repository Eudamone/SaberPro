package controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import service.FileProcessingService;
import utils.LocalMultipartFile;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class dashboardDeanController {

    private static final Logger log = LoggerFactory.getLogger(dashboardDeanController.class);

    @FXML
    private Button bttCreateUser;

    private FileProcessingService fileService;

    // No-arg constructor needed for FXMLLoader tooling/IDE
    public dashboardDeanController() {}

    @Autowired
    public dashboardDeanController(FileProcessingService fileService) {
        this.fileService = fileService;
    }

    @FXML
    public void initialize() {

    }

    @FXML
    public void onCargaMasiva() {
        // Mantener comportamiento original de la carga masiva de usuarios (no tocar aquí)
        // Este método queda vacío para no interferir con la implementación específica de carga de usuarios existente.
    }

    @FXML
    public void onCargaResultados() {
        // elegir tipo de archivo de resultados
        List<String> choices = new ArrayList<>();
        choices.add("Externo General");
        choices.add("Externo Específico");
        choices.add("Interno");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Carga de resultados");
        dialog.setHeaderText("Seleccione el tipo de archivo de resultados a cargar");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        String tipo = result.get();

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo(s) - Resultados");

        if ("Externo Específico".equals(tipo)) {
            List<File> files = fc.showOpenMultipleDialog(getWindow());
            if (files == null || files.isEmpty()) return;

            // pedir periodo (requerido)
            javafx.scene.control.TextInputDialog td = new javafx.scene.control.TextInputDialog();
            td.setTitle("Periodo");
            td.setHeaderText("Ingrese el periodo (ej: 20121)");
            Optional<String> p = td.showAndWait();
            if (p.isEmpty()) return;
            final int periodoFinal;
            try { periodoFinal = Integer.parseInt(p.get().trim()); } catch (Exception ex) { showAlert("Error","Periodo inválido"); return; }
            if (!fileService.existsGeneralPeriod(periodoFinal)) { showAlert("Error","No existe archivo general para el periodo " + periodoFinal); return; }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    for (File f : files) {
                        fileService.parseAndSaveSpecifics(new LocalMultipartFile(f, "text/plain"), periodoFinal);
                    }
                    return null;
                }
            };
            task.setOnSucceeded(ev -> showAlert("Éxito", "Archivos específicos de resultados procesados"));
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                if (ex != null) log.error("Error procesando archivos específicos", ex);
                showAlert("Error", "Error procesando archivos: " + (ex != null ? ex.getMessage() : "unknown"));
            });
            new Thread(task).start();

        } else if ("Externo General".equals(tipo)) {
            File file = fc.showOpenDialog(getWindow());
            if (file == null) return;
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    fileService.parseAndSaveGeneral(new LocalMultipartFile(file, "text/plain"));
                    return null;
                }
            };
            task.setOnSucceeded(ev -> showAlert("Éxito", "Archivo general procesado"));
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                if (ex != null) log.error("Error procesando archivo general", ex);
                showAlert("Error", "Error procesando archivo: " + (ex != null ? ex.getMessage() : "unknown"));
            });
            new Thread(task).start();

        } else if ("Interno".equals(tipo)) {
            File file = fc.showOpenDialog(getWindow());
            if (file == null) return;
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    fileService.parseAndSaveInternal(new LocalMultipartFile(file, "text/plain"));
                    return null;
                }
            };
            task.setOnSucceeded(ev -> showAlert("Éxito", "Archivo interno procesado"));
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                if (ex != null) log.error("Error procesando archivo interno", ex);
                showAlert("Error", "Error procesando archivo: " + (ex != null ? ex.getMessage() : "unknown"));
            });
            new Thread(task).start();
        }
    }

    private Window getWindow() {
        return bttCreateUser.getScene().getWindow();
    }

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
