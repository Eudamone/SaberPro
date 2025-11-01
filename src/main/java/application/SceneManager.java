package application;


import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import views.FxmlView;

import java.io.IOException;

public class SceneManager {
    private Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;

    // Constructor
    public SceneManager(Stage stage, SpringFXMLLoader loader){
        this.primaryStage = stage;
        this.springFXMLLoader = loader;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void switchScene(final FxmlView view) throws IOException {
        if (primaryStage == null) throw new IllegalStateException("Primary stage not set in SceneManager");
        Parent rootNode = loadRootNode(view.getFxmlFile());

        Scene scene = new Scene(rootNode);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Parent loadRootNode(String fxmlPath) throws IOException {
        Parent rootNode = null;
        try{
            rootNode = springFXMLLoader.load(fxmlPath);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        return rootNode;
    }

    public void switchToNextScene(final FxmlView view) throws IOException {
        if (primaryStage == null) throw new IllegalStateException("Primary stage not set in SceneManager");
        Parent rootNode = loadRootNode(view.getFxmlFile());
        primaryStage.getScene().setRoot(rootNode);

        primaryStage.show();
    }

}
