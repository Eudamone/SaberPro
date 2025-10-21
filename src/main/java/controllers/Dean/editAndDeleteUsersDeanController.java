package controllers.Dean;

import application.SceneManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.util.Callback;
import model.Decano;
import model.Usuario;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.UsuarioService;
import utils.Alerts;
import utils.NavigationHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static utils.Alerts.*;

@Component
public class editAndDeleteUsersDeanController {

    private final SceneManager sceneManager;

    // --- Servicios y Estado ---
    private final UsuarioService usuarioService;
    private Usuario selectedUser = null; // Usuario actualmente en edición



    @Lazy
    public editAndDeleteUsersDeanController(SceneManager sceneManager,UsuarioService usuarioService) {
        this.sceneManager = sceneManager;
        this.usuarioService = usuarioService;
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event, sceneManager);
    }

    @FXML
    private GridPane editForm;

    @FXML
    private ComboBox<String> cBoxFaculty;

    @FXML
    private ComboBox<String> cBoxRol;

    @FXML
    private ComboBox<String> cBoxTypeDocument;

    @FXML
    private ComboBox<String> cBoxTypeTeaching;

    @FXML
    private TableColumn<Usuario, Void> colActions;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, Usuario.rolType> colRol;

    @FXML
    private TableColumn<Usuario,String> colUsername;

    @FXML
    private Label lbCodeTeaching;

    @FXML
    private TextField tfCodeTeaching;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfName;

    @FXML
    private TextField tfNameUser;

    @FXML
    private TextField tfNewPassword;

    @FXML
    private TextField tfNumberDocument;

    @FXML
    private TextField tfRepeatNewPassword;

    @FXML
    private TableView<Usuario> usersTable;

    @FXML
    void cancelEdit(ActionEvent event) {

    }

    @FXML
    void saveChanges(ActionEvent event) {

    }

    @FXML
    public void initialize(){
        editForm.setVisible(false);

        // 1. Configurar Columnas (Bindings)
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        // 2. Cargar datos iniciales
        loadUsersData();

        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, new String[]{"Decano","Docente","Coordinador Saber Pro","Estudiante"});
        cBoxFaculty.getItems().addAll(list);
    }

    // --- Lógica de Edición y Formulario ---

    private void handleEditUser(Usuario user) {
        this.selectedUser = user;

        // 1. Mostrar el formulario
        editForm.setVisible(true);

        // 2. Llenar campos base (Usuario)
        tfNameUser.setText(user.getUsername());
        tfName.setText(user.getNombre());
        tfEmail.setText(user.getEmail());
        tfNumberDocument.setText(user.getNumIdentification());

        cBoxRol.setValue(user.getRol().toString());
        cBoxTypeDocument.setValue(user.getDocument().toString());

        // 3. Manejar campos específicos según el Rol
        clearConditionalFields(); // Limpia y oculta por defecto

        // Solo Docente/Decano tiene campos condicionales
        if (user.getRol() == Usuario.rolType.Decano.getTipo() || user.getRol() == Usuario.rolType.Docente.getTipo()) {

            // Buscar la entidad Decano (asumiendo que Docente usa la misma tabla Decano.java)
            Optional<Decano> decanoOpt = usuarioService.findDecanoById(user.getId());

            if (decanoOpt.isPresent()) {
                Decano decano = decanoOpt.get();

                // Mostrar y llenar campos condicionales
                lbCodeTeaching.setVisible(true);
                tfCodeTeaching.setVisible(true);
                cBoxTypeTeaching.setVisible(true);

                tfCodeTeaching.setText(decano.getCodeTeacher());
                cBoxTypeTeaching.setValue(decano.getTipoDocente().getEtiqueta()); // Usar getEtiqueta si es necesario

                // Asumiendo que la facultad es un campo en Decano/Docente o debe ser persistido aparte
                // cBoxFacultyEdit.setValue(decano.getFaculty());
            }
        }
    }
    private void loadUsersData() {
        // En un entorno real, esto debería ir en un Task asíncrono
        List<Usuario> users = usuarioService.findAllUsers();
        ObservableList<Usuario> observableUsers = FXCollections.observableArrayList(users);
        usersTable.setItems(observableUsers);
    }

    private void setupActionsColumn() {
        Callback<TableColumn<Usuario, Void>, TableCell<Usuario, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Usuario, Void> call(final TableColumn<Usuario, Void> param) {
                final TableCell<Usuario, Void> cell = new TableCell<>() {

                    private final Button btnEdit = new Button("Editar"); // Ícono de lápiz o editar
                    private final Button btnDelete = new Button("Borrar"); // Ícono de papelera o eliminar

                    {
                        // Estilo básico para los botones (opcional)
                        btnEdit.setStyle("-fx-padding: 3;");
                        btnDelete.setStyle("-fx-padding: 3;");

                        // Lógica del botón EDITAR
                        btnEdit.setOnAction(event -> {
                            Usuario data = getTableView().getItems().get(getIndex());
                            handleEditUser(data);
                        });

                        // Lógica del botón ELIMINAR
                        btnDelete.setOnAction(event -> {
                            Usuario data = getTableView().getItems().get(getIndex());
                            handleDeleteUser(data);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox box = new HBox(5, btnEdit, btnDelete);
                            box.setAlignment(Pos.CENTER);
                            setGraphic(box);
                        }
                    }
                };
                return cell;
            }
        };

        colActions.setCellFactory(cellFactory);
    }

    private void clearConditionalFields() {
        // Ocultar por defecto
        lbCodeTeaching.setVisible(false);
        tfCodeTeaching.setVisible(false);
        cBoxTypeTeaching.setVisible(false);

        // Limpiar
        tfCodeTeaching.clear();
        cBoxTypeTeaching.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleUpdateUser() {
        if (selectedUser == null) {
            showError("Error", "No hay usuario seleccionado para actualizar.");
            return;
        }

        // 1. Recoger datos de la UI
        Usuario updatedUser = new Usuario();
        // NOTA: Usar el ID del usuario seleccionado
        updatedUser.setId(selectedUser.getId());

        // Datos base
        updatedUser.setUsername(tfNameUser.getText());
        updatedUser.setNombre(tfName.getText());
        updatedUser.setEmail(tfEmail.getText());
        updatedUser.setNumIdentification(tfNumberDocument.getText());
        updatedUser.setDocument(Usuario.typeDocument.valueOf(cBoxTypeDocument.getValue()));
        updatedUser.setRol(Usuario.rolType.valueOf(cBoxRol.getValue()));

        // 2. Recoger datos condicionales
        String codeTeaching = tfCodeTeaching.getText();
        String typeTeaching = cBoxTypeTeaching.getValue();
        // String faculty = cBoxFacultyEdit.getValue(); // Si manejas facultad

        // 3. Llamar al servicio de actualización (ASÍNCRONO EN PRODUCCIÓN)
        try {
            // Se debe crear un nuevo método en UsuarioService para esto
            usuarioService.updateUserAndSpecializedEntity(updatedUser, codeTeaching, typeTeaching);
            showInformation( "Usuario " + updatedUser.getUsername() + " actualizado.","Éxito");

            // 4. Limpiar y recargar
            editForm.setVisible(false);
            loadUsersData();
            this.selectedUser = null;
        } catch (Exception e) {
            showError("Error de Actualización", "No se pudo actualizar el usuario: " + e.getMessage());
        }
    }

    // --- Lógica de Eliminación ---

    private void handleDeleteUser(Usuario user) {
        Optional<ButtonType> result = showConfirmationAndGetResult(
                "¿Está seguro de que desea eliminar al usuario " + user.getUsername() + " (" + user.getRol() + ")?",
                "Confirmar Eliminación"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 1. Llamar al servicio de eliminación (ASÍNCRONO EN PRODUCCIÓN)
            try {
                // Se debe crear un nuevo método en UsuarioService para esto
                usuarioService.deleteUser(user.getId());
                Alerts.showInformation("Eliminado", "Usuario " + user.getUsername() + " eliminado.");

                // 2. Recargar tabla
                loadUsersData();
            } catch (Exception e) {
                showError("Error de Eliminación", "No se pudo eliminar al usuario: " + e.getMessage());
            }
        }
    }
}
