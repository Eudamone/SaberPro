package dto;

public class Ranking {
    private Long posicion;
    private Long totalEstudiantes;

    public Ranking(Long posicion, Long totalEstudiantes) {
        this.posicion = posicion;
        this.totalEstudiantes = totalEstudiantes;
    }

    public Long getPosicion() {
        return posicion;
    }

    public Long getTotalEstudiantes() {
        return totalEstudiantes;
    }
}
