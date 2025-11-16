package utils;

import javafx.collections.FXCollections;
import javafx.scene.chart.*;

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

    public static BarChart<Number,String> boxChartPromedyProgram(){
        // Se definen ejes
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(300.0);
        xAxis.setTickUnit(50.0);
        xAxis.setAnimated(false);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(false);

        yAxis.setCategories(FXCollections.observableArrayList("Programa 1", "Programa 2", "Programa 3", "Programa 4", "Programa 5"));
        yAxis.setAnimated(false);

        final BarChart<Number,String> barChart = new BarChart(xAxis, yAxis);
        barChart.setTitle(null);
        barChart.setLegendVisible(false);


        // Datos de prueba temporales
        XYChart.Series<Number,String> series = new XYChart.Series<>();
        series.setName("Promedio Programa");
        series.getData().add(new XYChart.Data<>(158.0,"Programa 1"));
        series.getData().add(new XYChart.Data<>(190.0,"Programa 2"));
        series.getData().add(new XYChart.Data<>(250.0,"Programa 3"));
        series.getData().add(new XYChart.Data<>(210.0,"Programa 4"));
        series.getData().add(new XYChart.Data<>(120.0,"Programa 5"));

        barChart.getData().add(series);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setHorizontalGridLinesVisible(false);

        String css = ChartCreator.class.getResource("/styles/barChartProgram.css").toExternalForm();
        barChart.getStylesheets().add(css);

        return barChart;
    }
}
