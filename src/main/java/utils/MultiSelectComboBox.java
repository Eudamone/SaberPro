package utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectComboBox extends Region {

    private final Button button = new Button("Seleccionar opciones");
    private final PopupControl popup = new PopupControl();
    private final ListView<CheckBox> listView = new ListView<>();
    private final Tooltip tooltip = new Tooltip();

    public MultiSelectComboBox(String promtText,List<String> items) {

        button.setText(promtText);

        button.setPadding(new Insets(0,10,0,10));

        // Tooltip dinámico
        tooltip.setText("Sin selección");
        Tooltip.install(button, tooltip);

        // Configurar ListView
        for (String item : items) {
            CheckBox cb = new CheckBox(item);
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> updateTooltip());
            listView.getItems().add(cb);
        }

        listView.setPrefSize(180, 200); // Scroll automático

        // Configurar popup
        popup.setAutoHide(true);
        popup.getScene().setRoot(listView);

        // Acción para abrir y cerrar con animación
        button.setOnAction(e -> togglePopup());

        getStyleClass().add("multi-select-combo");
        listView.getStyleClass().add("multi-select-popup");

        getChildren().add(button);
    }

    private void togglePopup() {
        if (popup.isShowing()) {
            animatePopupClosing();
        } else {
            Bounds b = button.localToScreen(button.getBoundsInLocal());
            popup.show(button, b.getMinX(), b.getMaxY());
            animatePopupOpening();
        }
    }

    // Obtener seleccionados
    public List<String> getSelectedItems() {
        List<String> selected = new ArrayList<>();
        listView.getItems().stream()
                .filter(CheckBox::isSelected)
                .forEach(cb -> selected.add(cb.getText()));
        return selected;
    }

    // Ajustar tamaño del botón
    @Override
    protected void layoutChildren() {
        button.resizeRelocate(0, 0, getWidth(), getHeight());
    }

    @Override
    protected double computePrefWidth(double height) {
        return 180;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 30;
    }

    private void animatePopupOpening() {
        listView.setOpacity(0);
        listView.setTranslateY(5);

        Timeline fade = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(listView.opacityProperty(), 0),
                        new KeyValue(listView.translateYProperty(), 5)
                ),
                new KeyFrame(Duration.millis(160),
                        new KeyValue(listView.opacityProperty(), 1),
                        new KeyValue(listView.translateYProperty(), 0)
                )
        );

        fade.play();
    }

    private void animatePopupClosing() {
        Timeline fade = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(listView.opacityProperty(), 1),
                        new KeyValue(listView.translateYProperty(), 0)
                ),
                new KeyFrame(Duration.millis(160),
                        new KeyValue(listView.opacityProperty(), 0),
                        new KeyValue(listView.translateYProperty(), -5)
                )
        );

        fade.setOnFinished(ev -> popup.hide());
        fade.play();
    }

    private void updateTooltip() {
        List<String> selected = getSelectedItems();

        if (selected.isEmpty()) {
            tooltip.setText("Sin selección");
        } else {
            tooltip.setText(String.join(", ", selected));
        }
    }
}
