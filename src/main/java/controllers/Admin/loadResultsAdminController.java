package controllers.Admin;

import application.SceneManager;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.FileProcessingService;
import utils.Alerts;
import utils.LocalMultipartFile;
import utils.UtilsComboBox;


import java.io.File;
import java.util.List;
import java.util.Optional;

@Component
public class loadResultsAdminController {

    @FXML
    private ComboBox<String> comboChoice;
    @FXML
    private TextField tfPeriodo;
    @FXML
    private Label lbNameFile;
    @FXML
    private VBox overlayBox;
    @FXML
    private HBox contentBox;


    // Para el manejo de resultados
    private List<File> archivos;
    private File archivo;

    private SceneManager sceneManager;
    private FileProcessingService fileService;

    @Lazy
    public loadResultsAdminController(SceneManager sceneManager,FileProcessingService fileService) {
        this.sceneManager = sceneManager;
        this.fileService = fileService;
    }

    @FXML
    public  void initialize(){
        UtilsComboBox.comboBoxInitializer(comboChoice,UtilsComboBox.typesDocumentsResult);
    }

    @FXML
    void cargarDocumento(MouseEvent event) {
        String tipo = comboChoice.getValue();
        Node node = (Node) event.getSource();
        Window ventana = (Window) node.getScene().getWindow();
        if (tipo == null){
            Alerts.showError("Ingrese el tipo de resultado a cargar","Error");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo(s) - Resultados");
        switch (tipo) {
            case "Externo Específico" ->{
                archivos = fc.showOpenMultipleDialog(ventana);
                lbNameFile.setText(archivos.get(0).getName());
            }case "Externo General","Interno" ->{
                archivo = fc.showOpenDialog(ventana);
                lbNameFile.setText(archivo.getName());
            }
        }
    }

    @FXML
    void onCargaResultados(MouseEvent event) {
        String tipo = comboChoice.getValue();
        if(tipo == null && (archivo.exists() || archivos.isEmpty())){
            Alerts.showError("Faltan campos por completar","Error");
            return;
        }

        switch (tipo) {
            case "Externo Específico" ->{
                if(archivos == null || archivos.isEmpty()){
                    Alerts.showError("No se han cargado el archivo","Error");
                    return;
                }
                TextInputDialog td = new TextInputDialog();
                td.setTitle("Periodo");
                td.setHeaderText("Ingrese el periodo (año) (ej: 2021)");
                Optional<String> p = td.showAndWait();
                if (p.isEmpty()) return;
                final int periodoFinal;
                try { periodoFinal = Integer.parseInt(p.get().trim()); } catch (Exception ex) { Alerts.showError("Periodo inválido","Error"); return; }
                if (!fileService.existsGeneralPeriod(periodoFinal)) { Alerts.showWarning("No existen resultados generales para el periodo "+ periodoFinal,"Error"); return;}

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        for (File f : archivos) {
                            // Usar la nueva implementación que busca el resultado general y crea módulos si es necesario
                            fileService.parseAndSaveSpecificsResults(new LocalMultipartFile(f, "text/plain"), periodoFinal);
                        }
                        return null;
                    }
                };
                task.setOnRunning(e -> showOverlay());
                task.setOnSucceeded(ev -> {
                    hideOverlay();
                    cleanElements();
                    Alerts.showInformation("Archivos específicos de resultados procesados","Éxito");
                });
                task.setOnFailed(ev -> {
                    hideOverlay();
                    cleanElements();
                    Throwable ex = task.getException();
                    if (ex != null)
                        Alerts.showError("Error procesando archivos: " + (ex != null ? ex.getMessage() : "unknown"), "Error");
                });
                new Thread(task).start();
            }
            case "Externo General" ->{
                if (archivo == null) return;
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        fileService.parseAndSaveGeneral(new LocalMultipartFile(archivo, "text/plain"));
                        return null;
                    }
                };
                task.setOnRunning(e -> showOverlay());
                task.setOnSucceeded(ev -> {
                    hideOverlay();
                    cleanElements();
                    Alerts.showInformation("Archivo general procesado","Éxito");
                });
                task.setOnFailed(ev -> {
                    hideOverlay();
                    cleanElements();
                    Throwable ex = task.getException();
                    if (ex != null)
                        Alerts.showError("Error procesando archivo: " + (ex != null ? ex.getMessage() : "unknown"),"Error");
                });
                new Thread(task).start();
            }
            case "Interno" ->{
                if (archivo == null){
                    Alerts.showError("No se han cargado el archivo","Error");
                    return;
                }

                // Pedir periodo para el archivo interno
                TextInputDialog td = new TextInputDialog();
                td.setTitle("Periodo");
                td.setHeaderText("Ingrese el periodo (ej: 2021)");
                Optional<String> p = td.showAndWait();
                if (p.isEmpty()) return;
                final int periodoFinal;
                try {
                    periodoFinal = Integer.parseInt(p.get().trim());
                } catch (Exception ex) {
                    Alerts.showError("Periodo inválido","Error");
                    return;
                }

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        fileService.parseAndSaveInternal(new LocalMultipartFile(archivo, "text/plain"), periodoFinal);
                        return null;
                    }
                };
                task.setOnRunning(e -> showOverlay());
                task.setOnSucceeded(ev -> {
                    hideOverlay();
                    cleanElements();
                    Alerts.showInformation("Archivo interno procesado","Éxito" );
                });
                task.setOnFailed(ev -> {
                    hideOverlay();
                    cleanElements();
                    Throwable ex = task.getException();
                    if (ex != null)
                        Alerts.showError("Error procesando archivo: " + (ex != null ? ex.getMessage() : "unknown"),"Error");
                });
                new Thread(task).start();
            }
        }
    }

    @FXML
    void cancelLoad(MouseEvent event) {
        String tipo = comboChoice.getValue();
        if(tipo == null){return;}
        else{
            cleanElements();
        }
    }

    private void cleanElements(){
        lbNameFile.setText("Seleccione el archivo");
        if(archivo.exists() || archivos != null){
            archivo = null;
        }
        if(archivos != null){
            if(!archivos.isEmpty()){
                archivos.clear();
            }
        }
        UtilsComboBox.limpiarComboBox(comboChoice);
    }

    private void showOverlay(){
        overlayBox.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(200),overlayBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        contentBox.setEffect(new GaussianBlur(10));
    }

    private void hideOverlay(){
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlayBox);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> overlayBox.setVisible(false));
        ft.play();

        contentBox.setEffect(null);
    }
}
