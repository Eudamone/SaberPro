package model;

import jakarta.persistence.*;

@Entity
@Table(name = "resultado_modulo_interno")
public class InternalModuleResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interno_id", nullable = true)
    private Long internoId;

    @Column(name = "modulo_id", nullable = true)
    private Integer moduloId;

    @Column(name = "puntaje", nullable = true)
    private Integer puntaje;

    @Column(name = "percentil_nacional", nullable = true)
    private Integer percentilNacional;

    @Column(name = "percentil_grupo_referencia", nullable = true)
    private Integer percentilGrupoReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id",insertable = false, updatable = false)
    private Modulo modulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interno_id", referencedColumnName = "documento", insertable = false, updatable = false)
    private InternalResult internalResult;

    public InternalModuleResult() {}

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInternoId() { return internoId; }
    public void setInternoId(Long internoId) { this.internoId = internoId; }
    public Integer getModuloId() { return moduloId; }
    public void setModuloId(Integer moduloId) { this.moduloId = moduloId; }
    public Integer getPuntaje() { return puntaje; }
    public void setPuntaje(Integer puntaje) { this.puntaje = puntaje; }
    public Integer getPercentilNacional() { return percentilNacional; }
    public void setPercentilNacional(Integer percentilNacional) { this.percentilNacional = percentilNacional; }
    public Integer getPercentilGrupoReferencia() { return percentilGrupoReferencia; }
    public void setPercentilGrupoReferencia(Integer percentilGrupoReferencia) { this.percentilGrupoReferencia = percentilGrupoReferencia; }

    public InternalResult getInternalResult() {
        return internalResult;
    }

    public void setInternalResult(InternalResult internalResult) {
        this.internalResult = internalResult;
    }
}