package controllers.Dean;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import utils.NavigationHelper;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class editAndDeleteUsersDeanController {

    private final SceneManager sceneManager;

    @Lazy
    public editAndDeleteUsersDeanController(SceneManager sceneManager) {
        this.sceneManager = sceneManager;
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event, sceneManager);
    }

    @FXML
    private ComboBox<String> cBoxFaculty;

    @FXML
    private ComboBox<String> cBoxRol;

    @FXML
    private ComboBox<String> cBoxTypeDocument;

    @FXML
    private ComboBox<String> cBoxTypeTeaching;

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
    void cancelEdit(ActionEvent event) {

    }

    @FXML
    void saveChanges(ActionEvent event) {

    }

    @FXML
    public void initialize(){
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, new String[]{"Decano","Docente","Coordinador Saber Pro","Estudiante"});
        cBoxFaculty.getItems().addAll(list);
    }
}
