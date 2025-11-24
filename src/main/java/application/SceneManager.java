package application;


import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import views.FxmlView;

import java.io.IOException;

public class SceneManager {
    private final Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;

    // Constructor
    public SceneManager(Stage stage, SpringFXMLLoader loader){
        this.primaryStage = stage;
        this.springFXMLLoader = loader;
    }

    public void switchScene(final FxmlView view) throws IOException {
        Parent rootNode = loadRootNode(view.getFxmlFile());

        Scene scene = new Scene(rootNode);
        primaryStage.setScene(scene);
        primaryStage.setTitle(view.getTitle());
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
        Parent rootNode = loadRootNode(view.getFxmlFile());
        primaryStage.getScene().setRoot(rootNode);

        primaryStage.setTitle(view.getTitle());
        primaryStage.show();
    }

    public void switchSceneAsync(final FxmlView view, StackPane currentRoot){
        // Se crea el overlay
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        ProgressIndicator pi = new ProgressIndicator();
        overlay.getChildren().add(pi);

        currentRoot.getChildren().add(overlay);

        Node content = currentRoot.getChildren().get(0);

        // Efecto Blur
        GaussianBlur blur = new GaussianBlur(0);
        content.setEffect(blur);

        // Animación del blur
        Timeline blurIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
                new KeyFrame(Duration.millis(250), new KeyValue(blur.radiusProperty(), 12))
        );
        blurIn.play();

        // Fade-in del overlay
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Task para cargar la siguiente escena sin bloquear UI
        Task<Parent> loadTask = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                return springFXMLLoader.load(view.getFxmlFile());
            }
        };

        // 3. Cuando termine, cambiar la raíz
        loadTask.setOnSucceeded(ev -> {
            Parent newRoot = loadTask.getValue();

            // Cambiar escena antes del fade-out
            primaryStage.getScene().setRoot(newRoot);
            primaryStage.setTitle(view.getTitle());

            // Fade-out del overlay
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), overlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            // Blur-out
            Timeline blurOut = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 12)),
                    new KeyFrame(Duration.millis(250), new KeyValue(blur.radiusProperty(), 0))
            );

            fadeOut.setOnFinished(event -> {
                currentRoot.getChildren().remove(overlay);
                content.setEffect(null); // quita blur
            });

            fadeOut.play();
            blurOut.play();
        });

        // 4. Si falla, quitar overlay
        loadTask.setOnFailed(ev -> {
            // Quita blur y overlay aunque falle
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
            fadeOut.setOnFinished(event -> {
                currentRoot.getChildren().remove(overlay);
                currentRoot.setEffect(null);
            });
            fadeOut.play();

            ev.getSource().getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }
}
