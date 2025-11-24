package dto;

import utils.Normalized;

public class UniversidadPromedio {
    private String nombre;
    private Double promedio;

    UniversidadPromedio(String nombre, Double promedio){
        this.nombre = Normalized.primerMayuscula(nombre);
        this.promedio = Math.round(promedio * 100.0) / 100.0;
    }

    public String getNombre() {
        return nombre;
    }

    public Double getPromedio() {
        return promedio;
    }
}
