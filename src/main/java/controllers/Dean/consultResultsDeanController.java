package controllers.Dean;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.springframework.stereotype.Component;
import services.EmailService;
import services.N8NClientService;

@Component
public class consultResultsDeanController {

    private N8NClientService  n8NClientService;
    private final EmailService emailService;

    consultResultsDeanController(N8NClientService n8NClientService,EmailService emailService) {
        this.n8NClientService = n8NClientService;
        this.emailService = emailService;
    }

    @FXML
    void closeSession(ActionEvent event) {

    }

    @FXML
    void handleViewChange(ActionEvent event) {

    }

    @FXML
    public void initialize(){
        new Thread(() -> {
            emailService.sendPasswordResetEmail("daza.adrian09@gmail.com","abc12346587");
        }).start();

    }
}
