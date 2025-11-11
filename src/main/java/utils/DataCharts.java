package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.text.DecimalFormat;

public class DataCharts {
    public static ObservableList<XYChart.Series<Number,Number>> getPromedyData(){
        XYChart.Series<Number,Number> prom1 =  new XYChart.Series<>();
        prom1.getData().add(new XYChart.Data<>(2019,125));
        prom1.getData().add(new XYChart.Data<>(2020,155));
        prom1.getData().add(new XYChart.Data<>(2021,185));
        prom1.getData().add(new XYChart.Data<>(2022,225));
        prom1.getData().add(new XYChart.Data<>(2023,200));

        ObservableList<XYChart.Series<Number,Number>> data = FXCollections.observableArrayList();
        data.add(prom1);
        return data;
    }

    public static void setFormatInteger(NumberAxis xAxis){
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis){
            @Override
            public String toString(Number object) {
                DecimalFormat format = new DecimalFormat("0");
                return format.format(object.intValue());
            }
        });
    }
}
