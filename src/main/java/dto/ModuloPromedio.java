package dto;

public class ModuloPromedio{
    private String modulo;
    private Double promedio;

    ModuloPromedio(String modulo, Double promedio) {
        this.modulo = modulo;
        this.promedio = Math.round(promedio * 100.0) / 100.0;
    }

    public Double  getPromedio() {
        return promedio;
    }

    public String getModulo() {
        return modulo;
    }
}
