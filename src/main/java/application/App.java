package application;

import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import views.FxmlView;

import java.io.IOException;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "model")
@ComponentScan(basePackages = {"application", "controllers","spring.config","model","services","repository","factories","utils"})
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

        sceneManager = applicationContext.getBean(SceneManager.class,primaryStage);
        // Configuración de icono de ventana
        stage.getIcons().add(new Image(getClass().getResource("/images/logo_unillanos.png").toExternalForm()));

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
