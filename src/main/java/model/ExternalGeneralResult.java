package model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "resultado_externo")
public class ExternalGeneralResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "periodo", nullable = true)
    private Integer periodo;

    @Column(name = "est_consecutivo", nullable = true)
    private String estConsecutivo;

    @Column(name = "estu_discapacidad", nullable = true)
    private String estuDiscapacidad;

    @Column(name = "estu_inst_departamento", nullable = true)
    private String estuInstDepartamento;

    @Column(name = "estu_inst_municipio", nullable = true)
    private String estuInstMunicipio;

    @Column(name = "estu_nucleo_pregrado", nullable = true)
    private String estuNucleoPregrado;

    @Column(name = "estu_prgm_academico", nullable = true)
    private String estuPrgmAcademico;

    @Column(name = "estu_snies_prgmacademico", nullable = true)
    private Integer estuSniesPrgmacademico;

    @Column(name = "inst_cod_institucion", nullable = true)
    private Integer instCodInstitucion;

    @Column(name = "inst_nombre_institucion", nullable = true)
    private String instNombreInstitucion;

    // Los campos de módulos fueron normalizados a tablas separadas (modulo / resultado_modulo_externo).
    // Para evitar que Hibernate intente escribir columnas que ya no existen en resultado_externo
    // los marcamos como @Transient: seguiremos manteniendo estos campos en la entidad para procesarlos
    // en memoria y luego persistirlos en la tabla de módulos cuando corresponda.

    @Transient
    private BigDecimal modCompetenCiudadaPunt;

    @Transient
    private BigDecimal modCompetenCiudadaPnbc;

    @Transient
    private BigDecimal modCompetenCiudadaPnal;

    @Transient
    private BigDecimal modComuniEscritaPunt;

    @Transient
    private BigDecimal modComuniEscritaPnbc;

    @Transient
    private BigDecimal modComuniEscritaPnal;

    @Transient
    private BigDecimal modInglesPunt;

    @Transient
    private BigDecimal modInglesPnbc;

    @Transient
    private BigDecimal modInglesPnal;

    @Transient
    private BigDecimal modLecturaCriticaPunt;

    @Transient
    private BigDecimal modLecturaCriticaPnbc;

    @Transient
    private BigDecimal modLecturaCriticaPnal;

    @Transient
    private BigDecimal modRazonaCuantitatPunt;

    @Transient
    private BigDecimal modRazonaCuantitativoPnbc;

    @Transient
    private BigDecimal modRazonaCuantitativoPnal;

    @Column(name = "percentil_global", nullable = true)
    private BigDecimal percentilGlobal;

    @Column(name = "percentil_nbc", nullable = true)
    private BigDecimal percentilNbc;

    @Column(name = "punt_global", nullable = true)
    private BigDecimal puntGlobal;

    // created_at es NOT NULL en la BD
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public ExternalGeneralResult() {}

    // getters y setters (nombres coinciden con uso en servicios)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPeriodo() { return periodo; }
    public void setPeriodo(Integer periodo) { this.periodo = periodo; }

    public String getEstConsecutivo() { return estConsecutivo; }
    public void setEstConsecutivo(String estConsecutivo) { this.estConsecutivo = estConsecutivo; }

    public String getEstuDiscapacidad() { return estuDiscapacidad; }
    public void setEstuDiscapacidad(String estuDiscapacidad) { this.estuDiscapacidad = estuDiscapacidad; }

    public String getEstuInstDepartamento() { return estuInstDepartamento; }
    public void setEstuInstDepartamento(String estuInstDepartamento) { this.estuInstDepartamento = estuInstDepartamento; }

    public String getEstuInstMunicipio() { return estuInstMunicipio; }
    public void setEstuInstMunicipio(String estuInstMunicipio) { this.estuInstMunicipio = estuInstMunicipio; }

    public String getEstuNucleoPregrado() { return estuNucleoPregrado; }
    public void setEstuNucleoPregrado(String estuNucleoPregrado) { this.estuNucleoPregrado = estuNucleoPregrado; }

    public String getEstuPrgmAcademico() { return estuPrgmAcademico; }
    public void setEstuPrgmAcademico(String estuPrgmAcademico) { this.estuPrgmAcademico = estuPrgmAcademico; }

    public Integer getEstuSniesPrgmacademico() { return estuSniesPrgmacademico; }
    public void setEstuSniesPrgmacademico(Integer estuSniesPrgmacademico) { this.estuSniesPrgmacademico = estuSniesPrgmacademico; }

    public Integer getInstCodInstitucion() { return instCodInstitucion; }
    public void setInstCodInstitucion(Integer instCodInstitucion) { this.instCodInstitucion = instCodInstitucion; }

    public String getInstNombreInstitucion() { return instNombreInstitucion; }
    public void setInstNombreInstitucion(String instNombreInstitucion) { this.instNombreInstitucion = instNombreInstitucion; }

    public BigDecimal getModCompetenCiudadaPunt() { return modCompetenCiudadaPunt; }
    public void setModCompetenCiudadaPunt(BigDecimal v) { this.modCompetenCiudadaPunt = v; }

    public BigDecimal getModCompetenCiudadaPnbc() { return modCompetenCiudadaPnbc; }
    public void setModCompetenCiudadaPnbc(BigDecimal v) { this.modCompetenCiudadaPnbc = v; }

    public BigDecimal getModCompetenCiudadaPnal() { return modCompetenCiudadaPnal; }
    public void setModCompetenCiudadaPnal(BigDecimal v) { this.modCompetenCiudadaPnal = v; }

    public BigDecimal getModComuniEscritaPunt() { return modComuniEscritaPunt; }
    public void setModComuniEscritaPunt(BigDecimal v) { this.modComuniEscritaPunt = v; }

    public BigDecimal getModComuniEscritaPnbc() { return modComuniEscritaPnbc; }
    public void setModComuniEscritaPnbc(BigDecimal v) { this.modComuniEscritaPnbc = v; }

    public BigDecimal getModComuniEscritaPnal() { return modComuniEscritaPnal; }
    public void setModComuniEscritaPnal(BigDecimal v) { this.modComuniEscritaPnal = v; }

    public BigDecimal getModInglesPunt() { return modInglesPunt; }
    public void setModInglesPunt(BigDecimal v) { this.modInglesPunt = v; }

    public BigDecimal getModInglesPnbc() { return modInglesPnbc; }
    public void setModInglesPnbc(BigDecimal v) { this.modInglesPnbc = v; }

    public BigDecimal getModInglesPnal() { return modInglesPnal; }
    public void setModInglesPnal(BigDecimal v) { this.modInglesPnal = v; }

    public BigDecimal getModLecturaCriticaPunt() { return modLecturaCriticaPunt; }
    public void setModLecturaCriticaPunt(BigDecimal v) { this.modLecturaCriticaPunt = v; }

    public BigDecimal getModLecturaCriticaPnbc() { return modLecturaCriticaPnbc; }
    public void setModLecturaCriticaPnbc(BigDecimal v) { this.modLecturaCriticaPnbc = v; }

    public BigDecimal getModLecturaCriticaPnal() { return modLecturaCriticaPnal; }
    public void setModLecturaCriticaPnal(BigDecimal v) { this.modLecturaCriticaPnal = v; }

    public BigDecimal getModRazonaCuantitatPunt() { return modRazonaCuantitatPunt; }
    public void setModRazonaCuantitatPunt(BigDecimal v) { this.modRazonaCuantitatPunt = v; }

    public BigDecimal getModRazonaCuantitativoPnbc() { return modRazonaCuantitativoPnbc; }
    public void setModRazonaCuantitativoPnbc(BigDecimal v) { this.modRazonaCuantitativoPnbc = v; }

    public BigDecimal getModRazonaCuantitativoPnal() { return modRazonaCuantitativoPnal; }
    public void setModRazonaCuantitativoPnal(BigDecimal v) { this.modRazonaCuantitativoPnal = v; }

    public BigDecimal getPercentilGlobal() { return percentilGlobal; }
    public void setPercentilGlobal(BigDecimal percentilGlobal) { this.percentilGlobal = percentilGlobal; }

    public BigDecimal getPercentilNbc() { return percentilNbc; }
    public void setPercentilNbc(BigDecimal percentilNbc) { this.percentilNbc = percentilNbc; }

    public BigDecimal getPuntGlobal() { return puntGlobal; }
    public void setPuntGlobal(BigDecimal puntGlobal) { this.puntGlobal = puntGlobal; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

}
