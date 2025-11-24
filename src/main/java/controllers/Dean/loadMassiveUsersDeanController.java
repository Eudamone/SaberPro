package controllers.Dean;

import application.SceneManager;
import dto.EstudianteLoad;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.ExcelReaderService;
import services.UsuarioService;
import utils.NavigationHelper;

import java.io.File;
import java.util.List;

import static utils.Alerts.showError;
import static utils.Alerts.showInformation;

@Component
public class loadMassiveUsersDeanController {

    private final SceneManager sceneManager;

    @FXML
    private Button bttUploadFile;

    @FXML
    private ComboBox<String> cBoxUserType;

    @FXML
    private HBox dropZone;

    @FXML
    private VBox overlayCharge;

    @FXML
    private Label lbFile;

    @FXML
    private StackPane rootPane;

    private final UsuarioService usuarioService;
    private final ExcelReaderService excelReaderService;
    private File selectedFile;

    @Lazy
    loadMassiveUsersDeanController(SceneManager sceneManager,UsuarioService usuarioService,ExcelReaderService excelReaderService) {
        this.sceneManager = sceneManager;
        this.usuarioService = usuarioService;
        this.excelReaderService = excelReaderService;
    }

    @FXML
    void handleViewChange(ActionEvent actionEvent)throws Exception {
        NavigationHelper.handleViewChange(actionEvent, sceneManager,rootPane);
    }

    @FXML
    public void initialize() {
        cBoxUserType.getItems().add("Estudiante");
    }

    // Método para abrir el diálogo de selección de archivos
    @FXML
    public void openFileSelector(MouseEvent event) {
        // Obtenemos la ventana actual para el FileChooser
        Window owner = ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Archivo de Carga Masiva (.xlsx)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Archivos Excel XLSX", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            selectedFile = file;
            lbFile.setText(file.getName());
            bttUploadFile.setDisable(false);
        } else {
            selectedFile = null;
            lbFile.setText("Seleccione el archivo");
            bttUploadFile.setDisable(true);
        }
    }

    // Método principal para iniciar la carga
    @FXML
    public void uploadMassiveFile() {
        if (selectedFile == null) {
            showError("Por favor, selecciona un archivo XLSX.","Error de Archivo");
            return;
        }

        if(cBoxUserType.getValue() == null){
            showError("Por favor, selecciona un tipo de usuario","Error de usuario");
            return;
        }

        if (!cBoxUserType.getValue().equals("Estudiante")) {
            showInformation( "La carga masiva para otros roles está en desarrollo. Selecciona 'Estudiante'.","En Desarrollo");
            return;
        }

        // Ejecutar la tarea de lectura y guardado en un hilo separado
        MassiveUploadTask task = new MassiveUploadTask(selectedFile, cBoxUserType.getValue());

        // Bloquear UI
        bttUploadFile.setDisable(true);
        overlayCharge.setVisible(true);

        task.setOnSucceeded(e -> {
            overlayCharge.setVisible(false);
            bttUploadFile.setDisable(false);

            int count = task.getValue();

            showInformation( count + " estudiantes fueron cargados y notificados correctamente.","Carga Exitosa");
            // Limpiar la interfaz
            selectedFile = null;
            lbFile.setText("Seleccione el archivo");
        });

        task.setOnFailed(e -> {
            overlayCharge.setVisible(false);
            bttUploadFile.setDisable(false);
            Throwable exception = task.getException();

            // 1. **CAMBIO CLAVE**: Forzar la impresión de la traza de error en la consola
            System.err.println("--- ERROR FATAL EN CARGA MASIVA ---");
            exception.printStackTrace();
            System.err.println("------------------------------------");

            // 2. Intentar obtener el mensaje más específico
            String message;
            if (exception.getCause() != null && exception.getCause().getMessage() != null) {
                message = exception.getCause().getMessage();
            } else if (exception.getMessage() != null) {
                message = exception.getMessage();
            } else {
                message = "Error desconocido al procesar el archivo. Revisa la consola para más detalles.";
            }

            showError("Error de Carga Masiva", "El proceso falló: " + message);
        });

        new Thread(task).start();
    }

    // Clase Task anidada para la ejecución asíncrona
    private class MassiveUploadTask extends Task<Integer> {
        private final File file;
        private final String userType;

        public MassiveUploadTask(File file, String userType) {
            this.file = file;
            this.userType = userType;
        }

        @Override
        protected Integer call() throws Exception {
            updateMessage("Leyendo archivo Excel y mapeando datos...");
            List<EstudianteLoad> estudianteLoads = excelReaderService.readEstudents(file);
            // Procesar la lista completa, manejando errores de fila y contando los éxitos.
            updateMessage("Guardando " + estudianteLoads.size() + " usuarios y enviando credenciales...");
            return usuarioService.processMassiveStudents(estudianteLoads);
        }
    }
}
