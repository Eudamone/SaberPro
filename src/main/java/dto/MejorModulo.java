package dto;

public class MejorModulo {
    private String nombre;
    private Integer puntaje;

    public MejorModulo(String nombre, Integer puntaje){
        this.nombre = nombre;
        this.puntaje = puntaje;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getPuntaje() {
        return puntaje;
    }
}
