package dto;

public class PromedioProgram {
    private String programa;
    private Double promedio;

    PromedioProgram(String programa, Double promedio) {
        this.programa = programa;
        this.promedio = Math.round(promedio * 100.0) / 100.0;
    }

    public String getPrograma() {
        return programa;
    }

    public Double getPromedio() {
        return promedio;
    }
}
