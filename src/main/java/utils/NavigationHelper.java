package utils;

import application.SceneManager;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import model.Usuario;
import views.FxmlView;

public class NavigationHelper {

    public static void handleViewChange(ActionEvent event, SceneManager sceneManager, StackPane rootPane) throws Exception{
        try{
            //Se obtiene el botón que desencadena el evento
            Button button = (Button) event.getSource();
            // Se lee la propiedad userData del botón para saber a qué vista se dirige
            String viewName = (String) button.getUserData();
            if(viewName != null && !viewName.isEmpty()){
                // Se convierte a la constante FxmlView
                FxmlView targetView = FxmlView.valueOf(viewName);
                // Se cambia de escena
                sceneManager.switchSceneAsync(targetView,rootPane);
            }else{
                System.err.println("Error: El botón no tiene la propiedad 'userData' definida.");
            }
        }catch(ClassCastException e){
            System.err.println("Error: La fuente del evento no es un botón o el userData no es una cadena.");
        } catch(IllegalArgumentException e){
            System.err.println("Error: Valor de userData inválido. No coincide con ninguna constante en FxmlView.");
        }
    }

    public static void changeSceneByRol(Usuario.rolType rol, SceneManager sceneManager,StackPane rootPane) throws Exception{
        try{
            switch(rol){
                case Decano -> {
                    sceneManager.switchSceneAsync(FxmlView.DASHBOARD_DEAN,rootPane);
                }
                case Estudiante -> {
                    sceneManager.switchSceneAsync(FxmlView.RESULTS_STUDENT,rootPane);
                }
                default -> {
                    System.out.println("No existe escena principal para ese usuario");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
