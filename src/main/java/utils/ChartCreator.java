package utils;

import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class ChartCreator {
    public static LineChart<String, Number> createLineChart() {
        // Se definen ejes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis  yAxis = new NumberAxis();

        //xAxis.setAutoRanging(false);
        xAxis.setCategories(FXCollections.observableArrayList("2019","2020","2021","2022","2023","2024","2025"));
        //xAxis.setTickMarkVisible(false);
        xAxis.setAnimated(false);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(300);
        yAxis.setTickUnit(50.0);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(false);

        final LineChart<String,Number> lineChart = new LineChart(xAxis, yAxis);
        lineChart.setTitle(null);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(false);


        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Promedio Institucional");
        series.getData().add(new XYChart.Data<>("2019", 175.0));
        series.getData().add(new XYChart.Data<>("2020", 140.0));
        series.getData().add(new XYChart.Data<>("2021", 165.0));
        series.getData().add(new XYChart.Data<>("2022", 150.0));
        series.getData().add(new XYChart.Data<>("2023", 225.0));
        series.getData().add(new XYChart.Data<>("2024", 180.0));
        series.getData().add(new XYChart.Data<>("2025", 200.0));
        lineChart.getData().add(series);
        lineChart.setVerticalGridLinesVisible(false);

        String css = ChartCreator.class.getResource("/styles/lineChartPromedy.css").toExternalForm();
        lineChart.getStylesheets().add(css);

        return lineChart;
    }
}
