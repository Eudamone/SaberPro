package utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

public class UtilsComboBox {
    public static  <T> void limpiarComboBox(ComboBox<T> comboBox){
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
