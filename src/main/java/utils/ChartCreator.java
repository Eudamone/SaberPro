package utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

public class ChartCreator {


    public static LineChart<String, Number> createLineChart(List<String> periodos) {
        // Se definen ejes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis  yAxis = new NumberAxis();

        //xAxis.setAutoRanging(false);
        xAxis.setCategories(FXCollections.observableArrayList(periodos));
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
        lineChart.setCreateSymbols(true);

        lineChart.setVerticalGridLinesVisible(false);

        String css = ChartCreator.class.getResource("/styles/lineChartPromedy.css").toExternalForm();
        lineChart.getStylesheets().add(css);

        return lineChart;
    }

    public static BarChart<Number,String> boxChartPromedyProgram(List<String> programs){
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

        yAxis.setCategories(FXCollections.observableArrayList(programs));
        yAxis.setAnimated(false);
        yAxis.setTickLabelGap(10);

        final BarChart<Number,String> barChart = new BarChart(xAxis, yAxis);
        barChart.setTitle(null);
        barChart.setLegendVisible(false);
        barChart.setCategoryGap(10);
        barChart.setBarGap(3);

        barChart.setVerticalGridLinesVisible(false);
        barChart.setHorizontalGridLinesVisible(false);

        String css = ChartCreator.class.getResource("/styles/barChartProgram.css").toExternalForm();
        barChart.getStylesheets().add(css);

        return barChart;
    }

}
