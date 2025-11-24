package controllers.Dean;

import application.SceneManager;
import application.SessionContext;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import dto.UsuarioInfoDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.Facultad;
import model.Programa;
import model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.CatalogService;
import services.UsuarioService;
import utils.Alerts;
import utils.NavigationHelper;
import utils.UtilsComboBox;
import views.FxmlView;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static utils.Alerts.*;
import static utils.UtilsComboBox.comboBoxInitializer;
import static utils.UtilsComboBox.limpiarComboBox;
import static utils.generateNameUser.generateUsername;

@Component
public class editAndDeleteUsersDeanController {

    private final SceneManager sceneManager;

    // --- Servicios y Estado ---
    private final UsuarioService usuarioService;
    private final SessionContext sessionContext;
    private UsuarioInfoDTO selectedUser = null; // Usuario actualmente en edición
    private final CatalogService catalogService;


    @Lazy
    public editAndDeleteUsersDeanController(SceneManager sceneManager, UsuarioService usuarioService, CatalogService catalogService, SessionContext sessionContext) {
        this.sceneManager = sceneManager;
        this.usuarioService = usuarioService;
        this.catalogService = catalogService;
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
    private GridPane editForm;

    @FXML
    private ComboBox<Facultad> cBoxFaculty;

    @FXML
    private ComboBox<String> cBoxRol;

    @FXML
    private ComboBox<String> cBoxTypeDocument;

    @FXML
    private ComboBox<String> cBoxTypeTeaching;

    @FXML
    private ComboBox<String> cBoxStateAcademy;

    @FXML
    private ComboBox<Programa> cBoxProgram;

    @FXML
    private TableColumn<UsuarioInfoDTO, Void> colActions;

    @FXML
    private TableColumn<UsuarioInfoDTO, String> colEmail;

    @FXML
    private TableColumn<UsuarioInfoDTO, String> colNombre;

    @FXML
    private TableColumn<UsuarioInfoDTO, Usuario.rolType> colRol;

    @FXML
    private TableColumn<UsuarioInfoDTO, String> colUsername;

    @FXML
    private Label lbCodeTeaching;

    @FXML
    private Label lbCodeStudent;

    @FXML
    private TextField tfCodeEstudent;

    @FXML
    private TextField tfCodeTeaching;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfNameUser;

    @FXML
    private PasswordField tfNewPassword;

    @FXML
    private TextField tfNumberDocument;

    @FXML
    private PasswordField tfRepeatNewPassword;

    @FXML
    private TableView<UsuarioInfoDTO> usersTable;

    @FXML
    private StackPane rootPane;

    @FXML
    void cancelEdit(ActionEvent event) {
        clearElements();
        editForm.setVisible(false);
    }

    @FXML
    void saveChanges(ActionEvent event) {
        if (selectedUser == null) {
            Alerts.showWarning("No hay ningun usuario seleccionado para editar", "Atención");
            return;
        }

        try {
            Usuario userDB = usuarioService.findUser(selectedUser.getId(),selectedUser.getRol());

            // Validamos contraseñas
            String pass1 = tfNewPassword.getText();
            String pass2 = tfRepeatNewPassword.getText();

            if (!pass1.isEmpty() || !pass2.isEmpty()) {
                if (!pass1.equals(pass2)) {
                    Alerts.showWarning("Las contraseñas no coinciden", "Error de validación");
                    return;
                }
                userDB.setPass(usuarioService.getEncoder().encode(pass1));
            }

            // Actualizar campos base
            userDB.setUsername(tfNameUser.getText());
            userDB.setEmail(tfEmail.getText());
            userDB.setNombre(tfName.getText());
            userDB.setDocument(Usuario.typeDocument.valueOf(cBoxTypeDocument.getValue()));
            userDB.setNumIdentification(tfNumberDocument.getText());

            usuarioService.getUsuarioRepository().save(userDB);

            String nuevoRol = cBoxRol.getValue();

            usuarioService.updateUser(selectedUser.getId(),selectedUser.getRol(),nuevoRol, extraFormData());

            Alerts.showInformation("Usuario actualizado correctamente", "Éxito");
            catalogService.clearCacheUsuarios();
            loadUsersData();
            editForm.setVisible(false);
            clearElements();

        } catch (Exception e) {
            Alerts.showError("Error al guardar cambios: " + e.getMessage(), "Error");
            e.printStackTrace();
        }

    }

    private Map<String, Object> extraFormData() {
        Map<String, Object> data = new HashMap<>();

        switch (selectedUser.getRol()) {
            case Docente, Decano -> {
                data.put("codigo", tfCodeTeaching.getText());
                data.put("tipo", cBoxTypeTeaching.getValue());
                data.put("facultad", cBoxFaculty.getValue());
            }
            case Estudiante -> {
                data.put("codigo", tfCodeEstudent.getText());
                data.put("estado", cBoxStateAcademy.getValue());
                data.put("programa", cBoxProgram.getValue());
            }
            default -> {
            }
        }
        return data;
    }

    @FXML
    public void initialize() {
        editForm.setVisible(false);

        comboBoxInitializer(cBoxRol, UtilsComboBox.comboRol);
        comboBoxInitializer(cBoxTypeDocument,UtilsComboBox.comboTypeDocument);
        comboBoxInitializer(cBoxTypeTeaching,UtilsComboBox.comboTeacher);

        // Para cambiar el username automáticamente
        tfNameUser.textProperty().addListener((obs, oldVal, newVal) -> generateUsername(tfNameUser, tfName.getText(), tfNumberDocument.getText(), usuarioService));
        tfNumberDocument.textProperty().addListener((obs, oldVal, newVal) -> generateUsername(tfNameUser, tfName.getText(), tfNumberDocument.getText(), usuarioService));

        try {
            List<Facultad> facultades = catalogService.findAllFacultades();
            cBoxFaculty.setItems(FXCollections.observableArrayList(facultades));

            cBoxFaculty.setConverter(new StringConverter<Facultad>() {
                @Override
                public String toString(Facultad facultad) {
                    return (facultad != null) ? facultad.getCodeFaculty() : null;
                }

                @Override
                public Facultad fromString(String string) {
                    return null;
                }
            });

            // Añadir listener para evento de selección
            cBoxFaculty.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    facultySelected(newValue);
                } else {
                    cBoxProgram.getItems().clear(); // Limpiar el combobox de programas si quita la selección de la facultad
                }
            });
        } catch (Exception e) {
            showError("Error al cargar facultades: " + e.getMessage(), "Error en el proceso");
            e.printStackTrace();
        }

        // 1. Configurar Columnas (Bindings)
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colActions.setCellFactory(accionesCellFactory());

        //  Cargar datos iniciales
        loadUsersData();

        //setupActionsColumn();
        colActions.setStyle("-fx-alignment: center;");
    }

    private void facultySelected(Facultad facultad) {
        try {
            List<Programa> programas = catalogService.findAllProgramas(facultad);

            //  Se actualiza el combobox de programas
            cBoxProgram.setItems(FXCollections.observableArrayList(programas));
            cBoxProgram.getSelectionModel().clearSelection();

            cBoxProgram.setConverter(new StringConverter<Programa>() {
                @Override
                public String toString(Programa programa) {
                    return (programa != null) ? programa.getName() : null;
                }

                @Override
                public Programa fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            showError("Error al cargar programas: " + e.getMessage(), "Error en el proceso");
            e.printStackTrace();
        }
    }

    private Callback<TableColumn<UsuarioInfoDTO, Void>, TableCell<UsuarioInfoDTO, Void>> accionesCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<UsuarioInfoDTO, Void> call(final TableColumn<UsuarioInfoDTO, Void> param) {
                return new TableCell<>() {
                    // Iconos con FontAwesomeIconView
                    final FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE_ALT);
                    final FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);

                    //Contenedores para los iconos
                    private final Button btnEdit = new Button();
                    private final Button btnDelete = new Button();

                    final HBox btnBox = new HBox(10); // Contenedor para los iconos

                    {
                        editIcon.getStyleClass().addAll("glyph-icon", "icon-edit");
                        deleteIcon.getStyleClass().addAll("glyph-icon", "icon-delete");
                        editIcon.setSize("20px");
                        deleteIcon.setSize("20px");

                        btnEdit.setGraphic(editIcon);
                        btnDelete.setGraphic(deleteIcon);

                        btnEdit.setStyle("-fx-background-color: transparent;-fx-cursor: hand;");
                        btnDelete.setStyle("-fx-background-color: transparent;-fx-cursor: hand;");

                        btnBox.setAlignment(Pos.CENTER);
                        btnBox.setFillHeight(true);
                        btnBox.getChildren().addAll(btnEdit, btnDelete);

                        // Manejo de eventos
                        editIcon.setOnMouseClicked(event -> {
                            UsuarioInfoDTO user = getTableView().getItems().get(getIndex());
                            editUser(user);
                        });

                        deleteIcon.setOnMouseClicked(event -> {
                            UsuarioInfoDTO user = getTableView().getItems().get(getIndex());
                            deleteUser(user);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnBox);
                        }
                    }
                };
            }
        };
    }

    private void editUser(
            UsuarioInfoDTO usuario
    ) {
        selectedUser = usuario;
        Usuario userDB;
        try {
            userDB = usuarioService.findUser(usuario.getId(),usuario.getRol());
        } catch (Exception e) {
            showError("Error al cargar usuario: " + e.getMessage(), "Error en el proceso");
            return;
        }
        editForm.setVisible(true);

        // Cargar datos de la BD
        tfNameUser.setText(usuario.getUsername());
        tfEmail.setText(usuario.getEmail());
        tfName.setText(usuario.getName());
        cBoxTypeDocument.setValue(usuario.getDocument().name());
        tfNumberDocument.setText(usuario.getNumIdentification());
        cBoxRol.setValue(usuario.getRol().getTipo());

        //Se ocultan los campos específicos
        cBoxFaculty.setVisible(false);
        lbCodeTeaching.setVisible(false);
        lbCodeStudent.setVisible(false);
        tfCodeTeaching.setVisible(false);
        tfCodeEstudent.setVisible(false);
        cBoxTypeTeaching.setVisible(false);
        cBoxStateAcademy.setVisible(false);
        cBoxProgram.setVisible(false);

        // Cargar datos especializados
        switch (usuario.getRol()) {
            case Estudiante -> {
                if (userDB.getEstudiante() != null) {
                    cBoxFaculty.setVisible(true);
                    lbCodeStudent.setVisible(true);
                    tfCodeEstudent.setVisible(true);
                    cBoxProgram.setVisible(true);
                    cBoxStateAcademy.setVisible(true);

                    cBoxFaculty.setValue(userDB.getEstudiante().getInscripcion().getPrograma().getFacultad());
                    tfCodeEstudent.setText(userDB.getEstudiante().getCodeStudent());
                    cBoxProgram.setValue(userDB.getEstudiante().getInscripcion().getPrograma());
                    cBoxStateAcademy.setValue(userDB.getEstudiante().getAcademicStatus().name());
                }
            }
            case Decano -> {
                if (userDB.getDecano() != null) {
                    cBoxFaculty.setVisible(true);
                    lbCodeTeaching.setVisible(true);
                    tfCodeTeaching.setVisible(true);
                    cBoxTypeTeaching.setVisible(true);

                    cBoxFaculty.setValue(userDB.getDecano().getFacultad());
                    tfCodeTeaching.setText(userDB.getDecano().getCodeTeacher());
                    cBoxTypeTeaching.setValue(userDB.getDecano().getTipoDocente().getEtiqueta());
                }
            }
            case Docente -> {
                if (userDB.getDocente() != null) {
                    cBoxFaculty.setVisible(true);
                    lbCodeTeaching.setVisible(true);
                    tfCodeTeaching.setVisible(true);
                    cBoxTypeTeaching.setVisible(true);

                    cBoxFaculty.setValue(userDB.getDocente().getFacultad());
                    tfCodeTeaching.setText(userDB.getDocente().getCodeTeacher());
                    cBoxTypeTeaching.setValue(userDB.getDocente().getTypeTeacher().getEtiqueta());
                }
            }
            default -> {
            }
        }
    }

    private void deleteUser(UsuarioInfoDTO user) {
        Optional<ButtonType> result = showConfirmationAndGetResult(
                "¿Está seguro de que desea eliminar al usuario " + user.getUsername() + " (" + user.getRol() + ")?",
                "Confirmar Eliminación"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Se llama al método de eliminación en el servicio de usuario
            try {
                Usuario userDB = usuarioService.findUser(user.getId(),user.getRol());
                usuarioService.deleteUser(userDB);
                Alerts.showInformation("Usuario " + user.getUsername() + " eliminado.", "Eliminado");
                catalogService.clearCacheUsuarios();
                loadUsersData();
            } catch (Exception e) {
                showError("No se pudo eliminar al usuario: " + e.getMessage(), "Error de Eliminación");
            }
        }
    }

    private void loadUsersData() {
        catalogService.clearCacheUsuarios();
        List<UsuarioInfoDTO> users = catalogService.findAllUsersTable();
        ObservableList<UsuarioInfoDTO> observableUsers = FXCollections.observableArrayList(users);
        usersTable.setItems(observableUsers);
    }

    private void clearElements() {
        tfNameUser.setText("");
        tfEmail.setText("");
        tfNewPassword.setText("");
        tfName.setText("");
        tfRepeatNewPassword.setText("");
        tfCodeEstudent.setText("");
        tfNumberDocument.setText("");
        tfCodeTeaching.setText("");

        limpiarComboBox(cBoxRol);
        limpiarComboBox(cBoxFaculty);
        limpiarComboBox(cBoxTypeDocument);
        limpiarComboBox(cBoxTypeTeaching);
        limpiarComboBox(cBoxStateAcademy);
        limpiarComboBox(cBoxProgram);
    }

}
