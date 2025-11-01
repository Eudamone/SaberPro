package spring.config;

import application.SceneManager;
import application.SpringFXMLLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class AppConfig {

    @Bean
    @Lazy
    public SceneManager sceneManager(SpringFXMLLoader loader) {
        return new SceneManager(null, loader);
    }
}
