package model;

import jakarta.persistence.*;

@Entity
@Table(name = "resultado_modulo_externo")
public class ExternalSpecificResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "externa_id", nullable = true)
    private Long externaId; // FK a resultado_externo.id

    @Column(name = "periodo", nullable = true)
    private Integer periodo; // año/periodo ligado al general

    @Column(name = "est_consecutivo", nullable = true)
    private String estuConsecutivo;

    @Column(name = "modulo_id", nullable = true)
    private Integer moduloId;

    @Column(name = "result_nombreprueba", nullable = true)
    private String resultNombrePrueba;

    @Column(name = "puntaje", nullable = true)
    private Integer resultPuntaje;

    @Column(name = "percentil_nacional", nullable = true)
    private Integer percentilNacional;

    @Column(name = "percentil_nbc", nullable = true)
    private Integer percentilNbc;

    public ExternalSpecificResult() {}

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExternaId() { return externaId; }
    public void setExternaId(Long externaId) { this.externaId = externaId; }
    public Integer getPeriodo() { return periodo; }
    public void setPeriodo(Integer periodo) { this.periodo = periodo; }
    public String getEstuConsecutivo() { return estuConsecutivo; }
    public void setEstuConsecutivo(String estuConsecutivo) { this.estuConsecutivo = estuConsecutivo; }
    public Integer getModuloId() { return moduloId; }
    public void setModuloId(Integer moduloId) { this.moduloId = moduloId; }
    public String getResultNombrePrueba() { return resultNombrePrueba; }
    public void setResultNombrePrueba(String resultNombrePrueba) { this.resultNombrePrueba = resultNombrePrueba; }
    public Integer getResultPuntaje() { return resultPuntaje; }
    public void setResultPuntaje(Integer resultPuntaje) { this.resultPuntaje = resultPuntaje; }
    public Integer getPercentilNacional() { return percentilNacional; }
    public void setPercentilNacional(Integer percentilNacional) { this.percentilNacional = percentilNacional; }
    public Integer getPercentilNbc() { return percentilNbc; }
    public void setPercentilNbc(Integer percentilNbc) { this.percentilNbc = percentilNbc; }
}
