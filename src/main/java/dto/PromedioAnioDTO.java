package dto;

public class PromedioAnioDTO {

    private Integer anio;
    private Double promedio;

    public PromedioAnioDTO(Integer anio,Double promedio){
        this.anio=anio;
        this.promedio= Math.round(promedio * 100.0) / 100.0;
    }

    public Integer getAnio() {
        return anio;
    }

    public Double getPromedio() {
        return promedio;
    }

    public String getAnioString(){
        return anio.toString();
    }
}
