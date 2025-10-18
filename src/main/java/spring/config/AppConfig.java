package spring.config;

import application.SceneManager;
import application.SpringFXMLLoader;
import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;




@Configuration
public class AppConfig {

    private final SpringFXMLLoader springFXMLLoader;

    public AppConfig(SpringFXMLLoader springFXMLLoader){
        this.springFXMLLoader = springFXMLLoader;
    }

    @Bean
    @Lazy(value = true)
    public SceneManager sceneManager(Stage stage) {
        return new SceneManager(stage, springFXMLLoader);
    }
}
