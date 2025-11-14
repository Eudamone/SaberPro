package model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "resultado_interno")
public class InternalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "periodo", nullable = true)
    private Integer periodo;

    @Column(name = "tipo_documento", nullable = true)
    private String tipoDocumento;

    @Column(name = "documento", nullable = true)
    private Long documento;

    @Column(name = "nombre", nullable = true)
    private String nombre;

    @Column(name = "numero_registro", nullable = true)
    private String numeroRegistro;

    @Column(name = "tipo_evaluado", nullable = true)
    private String tipoEvaluado;

    @Column(name = "snies_programa", nullable = true)
    private String sniesPrograma;

    @Column(name = "programa", nullable = true)
    private String programa;

    @Column(name = "ciudad_id", nullable = true)
    private Integer ciudadId;



    @Column(name = "grupo_referencia", nullable = true)
    private String grupoReferencia;

    @Column(name = "puntaje_global", nullable = true)
    private Integer puntajeGlobal;

    @Column(name = "percentil_nacional_global", nullable = true)
    private Integer percentilNacionalGlobal;

    @Column(name = "percentil_grupo_referencia", nullable = true)
    private Integer percentilGrupoReferencia;

    @Column(name = "created_at", nullable = true)
    private OffsetDateTime createdAt;

    public InternalResult() {}

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getPeriodo() { return periodo; }
    public void setPeriodo(Integer periodo) { this.periodo = periodo; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public Long getDocumento() { return documento; }
    public void setDocumento(Long documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNumeroRegistro() { return numeroRegistro; }
    public void setNumeroRegistro(String numeroRegistro) { this.numeroRegistro = numeroRegistro; }
    public String getTipoEvaluado() { return tipoEvaluado; }
    public void setTipoEvaluado(String tipoEvaluado) { this.tipoEvaluado = tipoEvaluado; }
    public String getSniesPrograma() { return sniesPrograma; }
    public void setSniesPrograma(String sniesPrograma) { this.sniesPrograma = sniesPrograma; }
    public String getPrograma() { return programa; }
    public void setPrograma(String programa) { this.programa = programa; }
    public Integer getCiudadId() { return ciudadId; }
    public void setCiudadId(Integer ciudadId) { this.ciudadId = ciudadId; }

    public String getGrupoReferencia() { return grupoReferencia; }
    public void setGrupoReferencia(String grupoReferencia) { this.grupoReferencia = grupoReferencia; }
    public Integer getPuntajeGlobal() { return puntajeGlobal; }
    public void setPuntajeGlobal(Integer puntajeGlobal) { this.puntajeGlobal = puntajeGlobal; }
    public Integer getPercentilNacionalGlobal() { return percentilNacionalGlobal; }
    public void setPercentilNacionalGlobal(Integer percentilNacionalGlobal) { this.percentilNacionalGlobal = percentilNacionalGlobal; }
    public Integer getPercentilGrupoReferencia() { return percentilGrupoReferencia; }
    public void setPercentilGrupoReferencia(Integer percentilGrupoReferencia) { this.percentilGrupoReferencia = percentilGrupoReferencia; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

}
