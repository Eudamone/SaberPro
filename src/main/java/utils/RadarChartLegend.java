package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

/**
 * Componente de leyenda para el RadarChart
 * Muestra los nombres y colores de cada serie de datos
 */
public class RadarChartLegend extends FlowPane {
    public RadarChartLegend() {
        setAlignment(Pos.CENTER);
        setHgap(25);
        setVgap(10);
        setPadding(new Insets(10, 0, 0, 0));
    }

    /**
     * Actualiza la leyenda con las series del radar chart
     * @param series Lista de series de datos
     */
    public void updateLegend(List<DataSeries> series) {
        getChildren().clear();

        for (DataSeries s : series) {
            HBox legendItem = createLegendItem(s);
            getChildren().add(legendItem);
        }
    }

    /**
     * Crea un item de leyenda con línea de color y nombre
     */
    private HBox createLegendItem(DataSeries series) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);

        // Línea de color (como en tu imagen)
        Region colorLine = new Region();
        colorLine.setMinWidth(30);
        colorLine.setPrefWidth(30);
        colorLine.setMaxWidth(30);
        colorLine.setMinHeight(3);
        colorLine.setPrefHeight(3);
        colorLine.setMaxHeight(3);

        // Aplicar el color usando RGB
        Color color = series.getColor();
        String colorStyle = String.format(
                "-fx-background-color: rgb(%d, %d, %d);",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255)
        );
        colorLine.setStyle(colorStyle);

        // Nombre de la serie
        Label nameLabel = new Label(series.getName());
        nameLabel.setStyle(
                "-fx-text-fill: #666666; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-family: 'Arial';"
        );

        // Tooltip con información adicional
        if (!series.getValues().isEmpty()) {
            double avg = series.getValues().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            double max = series.getValues().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0);

            double min = series.getValues().stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0);

            String tooltipText = String.format(
                    "%s\nPromedio: %.1f\nMáximo: %.1f\nMínimo: %.1f",
                    series.getName(), avg, max, min
            );

            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setShowDelay(Duration.millis(300));
            Tooltip.install(item, tooltip);

            // Efecto hover
            item.setOnMouseEntered(e -> {
                item.setStyle("-fx-cursor: hand;");
                item.setOpacity(0.7);
            });
            item.setOnMouseExited(e -> {
                item.setStyle("");
                item.setOpacity(1.0);
            });
        }

        item.getChildren().addAll(colorLine, nameLabel);
        return item;
    }
}
