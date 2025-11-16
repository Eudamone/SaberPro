package utils;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DataSeries {
    private final String name;
    private final List<Double> values;
    private final Color color;

    public DataSeries(String name, List<Double> values, Color color) {
        this.name = name;
        this.values = values;
        this.color = color;
    }

    public DataSeries(String name, List<Double> values) {
        this(name,values,Color.BLUE);
    }

    public String getName() {
        return name;
    }

    public List<Double> getValues() {
        return new ArrayList<>(values);
    }

    public Color getColor() {
        return color;
    }

    public Double getValue(int index) {
        if(index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return 0.0;
    }

    public int size(){
        return values.size();
    }

    @Override
    public String toString() {
        return String.format("DataSeries{name='%s', values=%d, color=%s}",
                name, values.size(), color);
    }
}
