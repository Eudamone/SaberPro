package model;

import jakarta.persistence.*;

@Entity
@Table(name = "resultado_especifico_externo")
public class ExternalSpecificResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estu_consecutivo", nullable = false)
    private String estuConsecutivo; // FK a external_general_result(estu_consecutivo)

    @Column(name = "result_nombreprueba", nullable = false)
    private Integer resultNombrePrueba; // FK a modulo(id)

    @Column(name = "result_puntaje")
    private Integer resultPuntaje;

    public ExternalSpecificResult() {}

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstuConsecutivo() { return estuConsecutivo; }
    public void setEstuConsecutivo(String estuConsecutivo) { this.estuConsecutivo = estuConsecutivo; }

    public Integer getResultNombrePrueba() { return resultNombrePrueba; }
    public void setResultNombrePrueba(Integer resultNombrePrueba) { this.resultNombrePrueba = resultNombrePrueba; }

    public Integer getResultPuntaje() { return resultPuntaje; }
    public void setResultPuntaje(Integer resultPuntaje) { this.resultPuntaje = resultPuntaje; }
}

