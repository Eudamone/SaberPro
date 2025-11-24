package model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "resultado_modulo_externo")
public class ExternalModuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "externa_id", nullable = true)
    private Long externaId; // FK a resultado_externo.id

    @Column(name = "modulo_id", nullable = true)
    private Integer moduloId;

    // Cambiado a BigDecimal para permitir decimales en el puntaje
    @Column(name = "puntaje", nullable = true, precision = 10, scale = 4)
    private BigDecimal resultPuntaje;

    @Column(name = "percentil_nacional", nullable = true)
    private Integer percentilNacional;

    @Column(name = "percentil_nbc", nullable = true)
    private Integer percentilNbc;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "externa_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ExternalGeneralResult externalGeneralResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", referencedColumnName = "id_modulo", insertable = false, updatable = false)
    private Modulo modulo;

    public ExternalModuleResult() {}

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExternaId() { return externaId; }
    public void setExternaId(Long externaId) { this.externaId = externaId; }

    public Integer getModuloId() { return moduloId; }
    public void setModuloId(Integer moduloId) { this.moduloId = moduloId; }

    // getter/setter actualizado para BigDecimal
    public BigDecimal getResultPuntaje() { return resultPuntaje; }
    public void setResultPuntaje(BigDecimal resultPuntaje) { this.resultPuntaje = resultPuntaje; }

    public Integer getPercentilNacional() { return percentilNacional; }
    public void setPercentilNacional(Integer percentilNacional) { this.percentilNacional = percentilNacional; }
    public Integer getPercentilNbc() { return percentilNbc; }
    public void setPercentilNbc(Integer percentilNbc) { this.percentilNbc = percentilNbc; }

    public ExternalGeneralResult getExternalGeneralResult() { return externalGeneralResult; }
    public void setExternalGeneralResult(ExternalGeneralResult externalGeneralResult) { this.externalGeneralResult = externalGeneralResult; }

    public Modulo getModulo() { return modulo; }
    public void setModulo(Modulo modulo) { this.modulo = modulo; }
}