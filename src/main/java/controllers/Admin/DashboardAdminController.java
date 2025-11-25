package controllers.Admin;

import application.SceneManager;
import application.SessionContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import services.CatalogService;
import utils.NavigationHelper;
import utils.Normalized;
import views.FxmlView;

import java.io.IOException;

@Component
public class DashboardAdminController {
    @FXML
    private Label lbCantidadExternos;

    @FXML
    private Label lbCantidadInternos;

    @FXML
    private Label lbUsuarios;

    @FXML
    private StackPane rootPane;

    private final SceneManager sceneManager;
    private final SessionContext sessionContext;
    private final CatalogService catalogService;

    @Lazy
    public DashboardAdminController(SceneManager sceneManager, SessionContext sessionContext,CatalogService catalogService) {
        this.sceneManager = sceneManager;
        this.sessionContext = sessionContext;
        this.catalogService = catalogService;
    }

    @FXML
    public void initialize(){
        setupIndicators();
    }

    private void setupIndicators(){
        lbCantidadInternos.setText(
                Normalized.formatWithThousands(catalogService.sizeInternResults().toString())
        );
        lbCantidadExternos.setText(
                Normalized.formatWithThousands(catalogService.sizeExternalResults().toString())
        );
        lbUsuarios.setText(
                Normalized.formatWithThousands(catalogService.sizeUsers().toString())
        );
    }

    @FXML
    void handleViewChange(ActionEvent event) throws Exception {
        NavigationHelper.handleViewChange(event,sceneManager,rootPane);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        sessionContext.logout();
        sceneManager.switchToNextScene(FxmlView.LOGIN);
    }
}
