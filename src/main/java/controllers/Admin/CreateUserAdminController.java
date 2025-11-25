package controllers.Admin;

import application.SceneManager;
import application.SessionContext;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Facultad;
import model.Programa;
import model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import repository.FacultadRepository;
import services.ProgramaService;
import services.UsuarioService;
import utils.FormValidator;
import utils.NavigationHelper;
import utils.UtilsComboBox;
import views.FxmlView;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Alerts.showError;
import static utils.Alerts.showInformation;
import static utils.UtilsComboBox.cleanComboBox;
import static utils.UtilsComboBox.comboBoxInitializer;
import static utils.generateNameUser.generateUsername;

@Component
public class CreateUserAdminController {
    private final SessionContext sessionContext;
    @FXML
    private Button bttCreateUser;

    @FXML
    private VBox loadingOverlay;

    private final SceneManager sceneManager;
    private final UsuarioService usuarioService;
    private final FacultadRepository facultadRepository;
    private final ProgramaService programaService;

    @FXML
    private ComboBox<Facultad> cBoxFaculty;

    @FXML
    private ComboBox<String> cBoxRol;

    @FXML
    private ComboBox<String> cBoxTeaching; // Tipo de docente

    @FXML
    private ComboBox<String> cBoxTypeDocument; // Tipo de documento

    @FXML
    private Label lbMultiRol;

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
    private Label lbCodeEstudent;

    @FXML
    private TextField tfCodeEstudent;

    @FXML
    private ComboBox<Programa> cBoxProgram;

    @FXML
    private ComboBox<String> cBoxStateAcademy;

    @FXML
    private StackPane rootPane;

    @Lazy
    public CreateUserAdminController(SceneManager sceneManager, UsuarioService usuarioService, FacultadRepository facultadRepository, ProgramaService programaService, SessionContext sessionContext){
        this.sceneManager = sceneManager;
        this.usuarioService = usuarioService;
        this.facultadRepository = facultadRepository;
        this.programaService = programaService;
        this.sessionContext = sessionContext;
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event, sceneManager,rootPane);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        sessionContext.logout();
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }

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

        isValid &= FormValidator.validateComboBox(cBoxTypeDocument);

        // --- 2. Validaciones de ComboBox (Requerido)
        isValid &= FormValidator.validateComboBox(cBoxRol);

        if(cBoxFaculty.isVisible()){
            isValid &= FormValidator.validateComboBox(cBoxFaculty);
        }

        if(tfCodeTeaching.isVisible()){
            isValid &= FormValidator.validateTextField(tfCodeTeaching, FormValidator.getNoSpacesPattern());
        }

        if(cBoxProgram.isVisible()){
            isValid &= FormValidator.validateComboBox(cBoxProgram);
        }

        if(cBoxTeaching.isVisible()){
            isValid &= FormValidator.validateComboBox(cBoxTeaching);
        }

        if(tfCodeEstudent.isVisible()){
            isValid &= FormValidator.validateTextField(tfCodeEstudent, FormValidator.getNumericPattern());
        }

        if(cBoxStateAcademy.isVisible()){
            isValid &= FormValidator.validateComboBox(cBoxStateAcademy);
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

        lbMultiRol.setVisible(false);
        cBoxFaculty.setVisible(false);
        tfCodeTeaching.setVisible(false);
        cBoxProgram.setVisible(false);
        cBoxTeaching.setVisible(false);
        lbCodeEstudent.setVisible(false);
        tfCodeEstudent.setVisible(false);
        cBoxStateAcademy.setVisible(false);

        if(selectedRol == null){return;}

        switch (selectedRol){
            case "Estudiante" -> {
                cBoxFaculty.setVisible(true);
                cBoxProgram.setVisible(true);
                lbCodeEstudent.setVisible(true);
                tfCodeEstudent.setVisible(true);
                cBoxStateAcademy.setVisible(true);
            }
            case "Docente", "Decano" -> {
                cBoxFaculty.setVisible(true);
                lbMultiRol.setVisible(true);
                tfCodeTeaching.setVisible(true);
                cBoxTeaching.setVisible(true);
            }
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

        // Construimos el mapa dinámico de datos
        final Map<String,Object> datos = new HashMap<>();

        switch (rol){
            case "Estudiante" -> {
                datos.put("codigo",tfCodeEstudent.getText());
                datos.put("estado",cBoxStateAcademy.getValue());
                datos.put("programa",cBoxProgram.getValue());
            }
            case "Decano", "Docente" -> {
                datos.put("codigo",tfCodeTeaching.getText());
                datos.put("tipo",cBoxTeaching.getValue());
                datos.put("facultad",cBoxFaculty.getValue());
            }
            default ->{}
        }

        // Creamos una Task
        Task<Usuario> task = new Task<>(){
            @Override
            protected Usuario call() throws Exception {
                // Operación DB
                return usuarioService.createAndNotify(
                        username,email,name,typeDocument,numberDocument,rol,datos
                );
            }
        };

        // Manejo de la finalización exitosa
        task.setOnSucceeded(event -> {
            // Restaura la UI
            loadingOverlay.setVisible(false);
            bttCreateUser.setDisable(false);

            Usuario user = task.getValue();
            //usuarioService.setNewUser(user); // Se agrega el nuevo usuario

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


    @FXML
    public void initialize(){
        comboBoxInitializer(cBoxRol, UtilsComboBox.comboRol);
        comboBoxInitializer(cBoxTypeDocument,UtilsComboBox.comboTypeDocument);
        comboBoxInitializer(cBoxTeaching,UtilsComboBox.comboTeacher);
        comboBoxInitializer(cBoxStateAcademy,UtilsComboBox.comboStateAcademic);

        tfName.textProperty().addListener((obs,oldVal,newVal) -> generateUsername(tfNameUser,tfName.getText(),tfNumberDocument.getText(),usuarioService));
        tfNumberDocument.textProperty().addListener((obs,oldVal,newVal) -> generateUsername(tfNameUser,tfName.getText(),tfNumberDocument.getText(),usuarioService));

        try{
            List<Facultad> facultades = facultadRepository.findAllFacultades();
            cBoxFaculty.setItems(FXCollections.observableArrayList(facultades));

            cBoxFaculty.setConverter(new StringConverter<Facultad>() {
                @Override
                public String toString(Facultad facultad) {
                    return (facultad != null) ? facultad.getCodeFaculty() : null;
                }
                @Override
                public Facultad fromString(String string) {return null;}
            });

            // Añadir listener para evento de selección
            cBoxFaculty.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null){
                    facultySelected(newValue);
                }else{
                    cBoxProgram.getItems().clear(); // Limpiar el combobox de programas si se desselecciona la facultad
                }
            });
        } catch(Exception e){
            showError("Error al cargar facultades: " + e.getMessage(), "Error en el proceso");
            e.printStackTrace();
        }

        lbMultiRol.setVisible(false);
        cBoxFaculty.setVisible(false);
        cBoxProgram.setVisible(false);
        tfCodeTeaching.setVisible(false);
        cBoxTeaching.setVisible(false);
        lbCodeEstudent.setVisible(false);
        tfCodeEstudent.setVisible(false);
        cBoxStateAcademy.setVisible(false);
    }

    private void facultySelected(Facultad facultad){
        try{
            List<Programa> programas = programaService.findProgramsByFaculty(facultad);

            //  Se actualiza el combobox de programas
            cBoxProgram.setItems(FXCollections.observableArrayList(programas));
            cBoxProgram.getSelectionModel().clearSelection();

            cBoxProgram.setConverter(new StringConverter<Programa>() {
                @Override
                public String toString(Programa programa) {
                    return (programa != null) ? programa.getName() : null;
                }
                @Override
                public Programa fromString(String string) {return null;}
            });
        }catch(Exception e){
            showError("Error al cargar programas: "+e.getMessage(), "Error en el proceso");
            e.printStackTrace();
        }
    }

    private void cleanElements() {
        tfNameUser.getStyleClass().remove("comboBoxError");
        tfNameUser.setText("");
        tfEmailInstitutional.setText("");
        tfName.setText("");
        tfNumberDocument.setText("");
        tfCodeTeaching.setText("");
        tfCodeEstudent.setText("");

        List<ComboBox<?>> listaCombox = Arrays.asList(cBoxRol,cBoxTypeDocument,cBoxFaculty,cBoxProgram,cBoxTeaching,cBoxStateAcademy);
        cleanComboBox(listaCombox);
    }


    @FXML
    void cancelCreateUser(MouseEvent event) {
        cleanElements();

        lbMultiRol.setVisible(false);
        cBoxFaculty.setVisible(false);
        cBoxProgram.setVisible(false);
        tfCodeTeaching.setVisible(false);
        cBoxTeaching.setVisible(false);
        lbCodeEstudent.setVisible(false);
        tfCodeEstudent.setVisible(false);
        cBoxStateAcademy.setVisible(false);
    }

}
