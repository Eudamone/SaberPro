package application;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import views.FxmlView;

import java.io.IOException;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"application", "controllers", "service", "repository", "model", "spring.config", "utils", "views"})
@EntityScan(basePackages = {"model","application"})
@EnableJpaRepositories(basePackages = {"repository", "application"})
public class App extends Application{

    private ConfigurableApplicationContext applicationContext;
    private static Stage stage;
    private SceneManager sceneManager;

    @Override
    public void init(){
        applicationContext = new SpringApplicationBuilder(App.class).run();
    }

    public static void main(String[] args){
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        sceneManager = applicationContext.getBean(SceneManager.class);
        sceneManager.setPrimaryStage(primaryStage);
        // Configuración de icono de ventana
        //stage.getIcons().add(new Image(getClass().getResource("/images/logo_unillanos.png").toExternalForm()));

        stage.centerOnScreen();

        stage.setMinWidth(1300);
        stage.setMinHeight(720);

        showLoginScene();
    }

    private void showLoginScene() throws IOException {
        sceneManager.switchScene(FxmlView.LOGIN);
    }

    @Override
    public void stop(){
        applicationContext.close();
    }
}
