package utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;

public class UtilsComboBox {
    public final static String[] comboChartPromedy = {"Últimos 7 años","Todos los años"};
    public final static String[] comboRol = {"Estudiante","Administrador","Docente","Decano","Director Programa","Secretaria Acreditacion","Coordinador Saber Pro"};
    public final static String[] comboTeacher = {"Planta","Ocasional","Catedrático"};
    public final static String[] comboTypeDocument = {"CC","TI","CE"};
    public final static String[] comboStateAcademic = {"Activo","Egresado","Retirado"};
    public final static String[] typesDocumentsResult = {"Externo General","Externo Específico","Interno"};

    public static void comboBoxInitializer(ComboBox<String> comboBox, String [] items){
        ObservableList<String> dataList = FXCollections.observableArrayList(Arrays.asList(items));
        comboBox.setItems(dataList);
    }

    public static void cleanComboBox(List<ComboBox<?>>  comboBoxList){
        for (ComboBox<?> comboBox : comboBoxList){
            limpiarComboBox(comboBox);
        }
    }


    public static <T> void limpiarComboBox(ComboBox<T> comboBox){
        comboBox.getSelectionModel().clearSelection();
        comboBox.setValue(null);

        // Limpiar el estilo si tiene de error
        comboBox.getStyleClass().remove("comboBoxError");

        comboBox.setButtonCell(new ListCell<T>(){
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? comboBox.getPromptText() : item.toString());
                if(empty || item == null){
                    setText(comboBox.getPromptText());
                }else{
                    StringConverter<T> converter = comboBox.getConverter();
                    if (converter != null){
                        setText(converter.toString(item)); // Usa el convertir definido
                    }else{
                        setText(item.toString());
                    }
                }
            }
        });
    }


}
