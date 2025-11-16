package utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente RadarChart nativo para JavaFX
 *
 * Escala: El valor máximo (maxValue) representa el 100% del radio.
 * Por ejemplo, si maxValue = 300:
 *   - Un valor de 150 se dibuja al 50% del radio
 *   - Un valor de 300 se dibuja al 100% del radio (borde exterior)
 *   - Un valor de 450 se recorta al 100% (no sale del radar)
 */
public class RadarChart extends Canvas {
    private final List<String> labels = new ArrayList<>();
    private final List<DataSeries> series = new ArrayList<>();
    private final ObjectProperty<Color> gridColor = new SimpleObjectProperty<>(Color.rgb(200,200,200));
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> basePolygonColor = new SimpleObjectProperty<>(Color.rgb(240,240,240));

    private double maxValue = 400.0;
    private int levels = 5;
    private double centerX;
    private double centerY;
    private double radius;
    private boolean showScaleValues = true; // Se muestren valores de escala

    public RadarChart(double width, double height) {
        super(width, height);

        // Redibujar cuando el tamaño cambia
        widthProperty().addListener(evt->draw());
        heightProperty().addListener(evt->draw());

        // Propiedades observables
        gridColor.addListener(evt->draw());
        backgroundColor.addListener(evt->draw());
        basePolygonColor.addListener(evt->draw());
    }

    public void setLabels(List<String> labels){
        this.labels.clear();
        this.labels.addAll(labels);
        draw();
    }

    public void addSeries(DataSeries serie){
        series.add(serie);
        draw();
    }

    public void addSeries(String name,List<Double> values,Color color){
        series.add(new DataSeries(name,values,color));
        draw();
    }

    public void clearSeries(){
        series.clear();
        draw();
    }

    /**
     * Establece el valor máximo de la escala
     * Este valor representa el 100% del radio del radar
     * @param maxValue Valor máximo (ej: 300, 400, 500)
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        draw();
    }

    public void setLevels(int levels) {
        this.levels = levels;
        draw();
    }

    /**
     * Activa o desactiva la visualización de valores de escala
     */
    public void setShowScaleValues(boolean show) {
        this.showScaleValues = show;
        draw();
    }

    private void draw(){
        if(labels.isEmpty()){return;}

        GraphicsContext gc = getGraphicsContext2D();

        // Limpiar canvas
        gc.setFill(backgroundColor.get());
        gc.fillRect(0,0,getWidth(),getHeight());

        // Calcular dimensiones
        centerX = getWidth()/2;
        centerY = getHeight()/2;
        radius = Math.min(getWidth(),getHeight()) /3;

        // Dibujar grid y ejes
        drawGrid(gc);

        // Dibujar series de datos
        for (DataSeries s : series) {
            drawDataSeries(gc,s);
        }
        // Dibujar valores de escala si está activado
        if(showScaleValues){
            drawScaleValues(gc);
        }

        // Se dibujan etiquetas al final para que queden encima
        drawLabels(gc);
    }

    // Dibuja los círculos concéntricos del grid
    private void drawGrid(GraphicsContext gc){
        gc.setStroke(gridColor.get());
        gc.setLineWidth(1);

        int n = labels.size();

        for(int i=1;i<=levels;i++){
            double r = radius * i /  levels;
            double[] xPoints = new double[n];
            double[] yPoints = new double[n];

            // Calcular puntos del polígono para este nivel
            for(int j = 0 ; j < n ;j++){
                double angle = Math.PI / 2 - (2 * Math.PI * j / n);
                xPoints[j] = centerX + r * Math.cos(angle);
                yPoints[j] = centerY - r * Math.sin(angle);
            }
            gc.strokePolygon(xPoints,yPoints,n);
        }
    }


    /**
     * Dibuja los valores de escala en el interior del radar
     * Muestra el valor correspondiente a cada nivel del pentágono
     */
    private void drawScaleValues(GraphicsContext gc) {
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/Montserrat-Regular.ttf");
            if(is != null){
                gc.setFont(Font.loadFont(is, 7));
                is.close();
            } else {
                gc.setFont(Font.font("Arial", 7));
            }
        } catch (Exception e) {
            gc.setFont(Font.font("Arial", 7));
        }

        gc.setFill(Color.rgb(140, 140, 140));
        gc.setTextAlign(TextAlignment.CENTER);

        // Dibujar valores en cada nivel del pentágono
        // Los colocamos en el centro-superior de cada nivel
        for(int level = 1; level <= levels; level++){
            double r = radius * level / levels;
            double value = (maxValue * level) / levels;

            // Posición: ligeramente hacia arriba desde el centro
            // Para que queden dentro del radar y no se superpongan con nada
            double angle = Math.PI / 2; // Ángulo superior
            double x = centerX;
            double y = centerY - r + 15; // Justo debajo del vértice superior

            // Dibujar el valor
            String valueText = String.format("%.0f", value);


            // Texto
            gc.setFill(Color.rgb(100, 100, 100));
            gc.fillText(valueText, x, y);
        }
    }

    // Dibuja las etiquetas en los extremos de los ejes
    private void drawLabels(GraphicsContext gc) {
        // Cargar fuente personalizada con manejo de errores
        try {
            InputStream is = getClass().getResourceAsStream("/fonts/Montserrat-Regular.ttf");
            if(is != null){
                gc.setFont(Font.loadFont(is,11));
                is.close();
            }else{
                System.out.println("La fuente Montserrat esta vacía");
                gc.setFont(Font.font("Arial",11));
            }
        }catch (Exception e){
            // Si falla se usa una fuente por defecto
            System.out.println("Se uso la fuente por defecto");
            gc.setFont(Font.font("Arial",11));
        }

        gc.setFill(Color.rgb(80, 80, 80));

        int n = labels.size();
        for (int i = 0; i < n; i++) {
            double angle = Math.PI / 2 - (2 * Math.PI * i / n);
            // Posición etiquetas
            double x = centerX + (radius - 5) * Math.cos(angle);
            double y = centerY - (radius - 5) * Math.sin(angle);

            String label = labels.get(i);

            // Ajustar alineación según posición
            if (Math.abs(Math.cos(angle)) < 0.1) {
                // Top o bottom - centrado
                gc.setTextAlign(TextAlignment.CENTER);
                if (Math.sin(angle) > 0) {
                    gc.fillText(label, x, y - 10); // Arriba
                } else {
                    gc.fillText(label, x, y + 20); // Abajo
                }
            } else if (Math.cos(angle) > 0) {
                // Derecha
                gc.setTextAlign(TextAlignment.LEFT);
                gc.fillText(label, x + 10, y + 5);
            } else {
                // Izquierda
                gc.setTextAlign(TextAlignment.RIGHT);
                gc.fillText(label, x - 10, y + 5);
            }

        }
    }

    // Dibuja una serie de datos como polígono
    private void drawDataSeries(GraphicsContext gc, DataSeries series) {
        if (series.getValues().isEmpty()) return;

        int n = labels.size();
        double[] xPoints = new double[n];
        double[] yPoints = new double[n];

        // Calcular puntos del polígono
        for (int i = 0; i < n && i < series.getValues().size(); i++) {
            double value = series.getValues().get(i);
            double normalizedValue = Math.min(value / maxValue, 1.0);
            double angle = Math.PI / 2 - (2 * Math.PI * i / n);

            xPoints[i] = centerX + radius * normalizedValue * Math.cos(angle);
            yPoints[i] = centerY - radius * normalizedValue * Math.sin(angle);
        }

        // Dibujar área rellena con transparencia
        gc.setFill(series.getColor().deriveColor(0, 1, 1, 0.05));
        gc.fillPolygon(xPoints, yPoints, n);

        // Dibujar borde del polígono
        gc.setStroke(series.getColor());
        gc.setLineWidth(1.5);
        gc.strokePolygon(xPoints, yPoints, n);

    }

    // Getters y setters para propiedades
    public ObjectProperty<Color> gridColorProperty() { return gridColor; }
    public Color getGridColor() { return gridColor.get(); }
    public void setGridColor(Color color) { gridColor.set(color); }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }
    public Color getBackgroundColor() { return backgroundColor.get(); }
    public void setBackgroundColor(Color color) { backgroundColor.set(color); }

    public List<DataSeries> getSeries() { return new ArrayList<>(series); }
}
