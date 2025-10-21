package controllers.Dean;

import application.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.UsuarioService;
import utils.FormValidator;

import java.util.ArrayList;
import java.util.List;

import static utils.Alerts.*;

@Component
public class createUserDeanController {

    @FXML
    private Button bttCreateUser;

    @FXML
    private VBox loadingOverlay;

    private final SceneManager sceneManager;
    private final UsuarioService usuarioService;

    @Lazy
    public createUserDeanController(SceneManager sceneManager, UsuarioService usuarioService) {
        this.sceneManager = sceneManager;
        this.usuarioService = usuarioService;
    };

    @FXML
    private ComboBox<String> cBoxFaculty;

    @FXML
    private ComboBox<String> cBoxRol;

    @FXML
    private ComboBox<String> cBoxTeaching; // Tipo de docente

    @FXML
    private ComboBox<String> cBoxTypeDocument; // Tipo de documento

    @FXML
    private Label lbCodeTeaching;

    @FXML
    private TextField tfCodeTeaching;

    @FXML
    private TextField tfEmailInstitutional;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfNameUser;

    @FXML
    private TextField tfNumberDocument;

    @FXML
    public void createUser(ActionEvent actionEvent) {
        boolean isValid = true;

        // --- 1. Validaciones de Texto y Formato

        // Nombre de Usuario: Requerido Y NO debe tener espacios
        isValid &= FormValidator.validateTextField(tfNameUser, FormValidator.getNoSpacesPattern());

        // Email Institucional: Requerido Y debe ser un email válido
        isValid &= FormValidator.validateTextField(tfEmailInstitutional, FormValidator.getEmailPattern());


        // Nombre Completo: Requerido (sin patrón específico)
        isValid &= FormValidator.validateTextField(tfName, null);

        // Número de Documento: Requerido Y debe ser numérico
        isValid &= FormValidator.validateTextField(tfNumberDocument, FormValidator.getNumericPattern());

        // --- 2. Validaciones de ComboBox (Requerido)
        isValid &= FormValidator.validateComboBox(cBoxRol);
        isValid &= FormValidator.validateComboBox(cBoxFaculty);
        isValid &= FormValidator.validateComboBox(cBoxTypeDocument);

        // --- 3. Validación Condicional (Lógica de Docente/Decano)
        String rolValue = cBoxRol.getValue();

        if (rolValue != null && !rolValue.equals("Estudiante")) {
            if (tfCodeTeaching.isVisible()) {
                // Código de Docente: Requerido Y debe ser numérico
                isValid &= FormValidator.validateTextField(tfCodeTeaching, FormValidator.getNumericPattern());
                // Tipo de Docente: Requerido
                isValid &= FormValidator.validateComboBox(cBoxTeaching);
            }
        }

        // --- 4. Resultado final
        if (isValid) {
            System.out.println("Formulario OK: Enviando correo...");
            createUserTask();
            cleanElements();
        } else {
            System.err.println("Formulario Inválido. Revise los campos resaltados y las reglas de formato.");
        }
    }

    @FXML
    void checkRol(ActionEvent event) {
        String selectedRol = cBoxRol.getSelectionModel().getSelectedItem();
        if (selectedRol != null && !selectedRol.equals("Estudiante")) {
            lbCodeTeaching.setVisible(true);
            tfCodeTeaching.setVisible(true);
            cBoxTeaching.setVisible(true);
        }else{
            lbCodeTeaching.setVisible(false);
            tfCodeTeaching.setVisible(false);
            cBoxTeaching.setVisible(false);

            tfCodeTeaching.getStyleClass().add("fieldText");
            cBoxTeaching.getStyleClass().remove("comboBox");
        }
    }

    private void createUserTask(){
        // Se bloquea la UI y mostrar la superposición (Hilo principal)
        // Deshabilitamos el botón y hacemos visible el overlay que bloquea los clics
        bttCreateUser.setDisable(true);
        loadingOverlay.setVisible(true);

        // Capturan valores de campos
        final String username =  tfNameUser.getText();
        final String email = tfEmailInstitutional.getText();
        final String name = tfName.getText();
        final String typeDocument = cBoxTypeDocument.getValue();
        final String numberDocument = tfNumberDocument.getText();
        final String rol = cBoxRol.getValue();

        // Creamos una Task
        Task<Usuario> task = new Task<>(){
            @Override
            protected Usuario call() throws Exception {
                // Operación DB
                return usuarioService.createAndNotify(
                        username,email,name,typeDocument,numberDocument,rol
                );
            }
        };

        // Manejo de la finalización exitosa
        task.setOnSucceeded(event -> {
            // Restaura la UI
            loadingOverlay.setVisible(false);
            bttCreateUser.setDisable(false);

            Usuario user = task.getValue();

            if(user != null){
                showInformation("Se envió el correo con las credenciales a "+ email +" con éxito","Correo enviado con éxito");
            }
        });

        // Manejo de posibles errores
        task.setOnFailed(event -> {
            // Restaura la UI
            loadingOverlay.setVisible(false);
            bttCreateUser.setDisable(false);

            Throwable exception = task.getException();
            System.err.println("Error en la tarea asíncrona: " + exception.getMessage());

            showError("Se produjo un error al enviar las credenciales o al guardar el usuario: " + exception.getMessage(), "Error en el proceso");
        });

        // Ejecutar la tarea
        new Thread(task).start();
    }

    private String[] comboFaculty = {"FCBI","FCAR","FCS","FCHE"};
    private String[] comboRol = {"Estudiante","Administrador","Docente","Decano","Director Programa","Secretaria Acreditacion","Coordinador Saber Pro"};
    private String[] comboTeacher = {"Planta","Ocasional","Catedrático"};
    private String[] comboTypeDocument = {"CC","TI","CE"};

    public void comboBox(ComboBox<String> comboBox,String [] items){
        List<String> list = new ArrayList<>();
        for(String data:items){
            list.add(data);
        }
        ObservableList<String> dataList = FXCollections.observableArrayList(list);
        comboBox.setItems(dataList);
    };

    @FXML
    public void initialize(){
        comboBox(cBoxFaculty,comboFaculty);
        comboBox(cBoxRol,comboRol);
        comboBox(cBoxTypeDocument,comboTypeDocument);
        comboBox(cBoxTeaching,comboTeacher);

        lbCodeTeaching.setVisible(false);
        tfCodeTeaching.setVisible(false);
        cBoxTeaching.setVisible(false);
    }

    private void cleanElements() {
        tfNameUser.setText("");
        tfEmailInstitutional.setText("");
        tfName.setText("");
        tfNumberDocument.setText("");
        tfCodeTeaching.setText("");
        cBoxRol.getSelectionModel().clearSelection();
        cBoxFaculty.getSelectionModel().clearSelection();
        cBoxTypeDocument.getSelectionModel().clearSelection();
        cBoxTeaching.getSelectionModel().clearSelection();
    }

}
