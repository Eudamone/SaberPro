package utils;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import views.FxmlView;

public class NavigationHelper {

    public static void handleViewChange(ActionEvent event, SceneManager sceneManager) throws Exception{
        try{
            //Se obtiene el botón que desencadena el evento
            Button button = (Button) event.getSource();
            // Se lee la propiedad userData del botón para saber a qué vista se dirige
            String viewName = (String) button.getUserData();
            if(viewName != null && !viewName.isEmpty()){
                // Se convierte a la constante FxmlView
                FxmlView targetView = FxmlView.valueOf(viewName);
                // Se cambia de escena
                sceneManager.switchToNextScene(targetView);
            }else{
                System.err.println("Error: El botón no tiene la propiedad 'userData' definida.");
            }
        }catch(ClassCastException e){
            System.err.println("Error: La fuente del evento no es un botón o el userData no es una cadena.");
        } catch(IllegalArgumentException e){
            System.err.println("Error: Valor de userData inválido. No coincide con ninguna constante en FxmlView.");
        }
    }
}
