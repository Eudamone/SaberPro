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

    // Guardaremos los IDs normalizados en la tabla resultado_externo
    // (se asume que la tabla en la BD tiene estos columnas con tipo numérico)
    @Column(name = "estu_inst_departamento", nullable = true)
    private Integer estuInstDepartamento;

    @Column(name = "estu_inst_municipio", nullable = true)
    private Integer estuInstMunicipio;

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

    // módulos y percentiles: cambiados a BigDecimal para preservar decimales
    @Column(name = "mod_competen_ciudada_punt", nullable = true)
    private BigDecimal modCompetenCiudadaPunt;

    @Column(name = "mod_competen_ciudada_pnbc", nullable = true)
    private BigDecimal modCompetenCiudadaPnbc;

    @Column(name = "mod_competen_ciudada_pnal", nullable = true)
    private BigDecimal modCompetenCiudadaPnal;

    @Column(name = "mod_comuni_escrita_punt", nullable = true)
    private BigDecimal modComuniEscritaPunt;

    @Column(name = "mod_comuni_escrita_pnbc", nullable = true)
    private BigDecimal modComuniEscritaPnbc;

    @Column(name = "mod_comuni_escrita_pnal", nullable = true)
    private BigDecimal modComuniEscritaPnal;

    @Column(name = "mod_ingles_punt", nullable = true)
    private BigDecimal modInglesPunt;

    @Column(name = "mod_ingles_pnbc", nullable = true)
    private BigDecimal modInglesPnbc;

    @Column(name = "mod_ingles_pnal", nullable = true)
    private BigDecimal modInglesPnal;

    @Column(name = "mod_lectura_critica_punt", nullable = true)
    private BigDecimal modLecturaCriticaPunt;

    @Column(name = "mod_lectura_critica_pnbc", nullable = true)
    private BigDecimal modLecturaCriticaPnbc;

    @Column(name = "mod_lectura_critica_pnal", nullable = true)
    private BigDecimal modLecturaCriticaPnal;

    @Column(name = "mod_razona_cuantitat_punt", nullable = true)
    private BigDecimal modRazonaCuantitatPunt;

    @Column(name = "mod_razona_cuantitativo_pnbc", nullable = true)
    private BigDecimal modRazonaCuantitativoPnbc;

    @Column(name = "mod_razona_cuantitativo_pnal", nullable = true)
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

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getPeriodo() { return periodo; }
    public void setPeriodo(Integer periodo) { this.periodo = periodo; }
    public String getEstConsecutivo() { return estConsecutivo; }
    public void setEstConsecutivo(String estConsecutivo) { this.estConsecutivo = estConsecutivo; }
    public String getEstuDiscapacidad() { return estuDiscapacidad; }
    public void setEstuDiscapacidad(String estuDiscapacidad) { this.estuDiscapacidad = estuDiscapacidad; }
    public Integer getEstuInstDepartamento() { return estuInstDepartamento; }
    public void setEstuInstDepartamento(Integer estuInstDepartamento) { this.estuInstDepartamento = estuInstDepartamento; }
    public Integer getEstuInstMunicipio() { return estuInstMunicipio; }
    public void setEstuInstMunicipio(Integer estuInstMunicipio) { this.estuInstMunicipio = estuInstMunicipio; }
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
    public void setModCompetenCiudadaPunt(BigDecimal modCompetenCiudadaPunt) { this.modCompetenCiudadaPunt = modCompetenCiudadaPunt; }
    public BigDecimal getModCompetenCiudadaPnbc() { return modCompetenCiudadaPnbc; }
    public void setModCompetenCiudadaPnbc(BigDecimal modCompetenCiudadaPnbc) { this.modCompetenCiudadaPnbc = modCompetenCiudadaPnbc; }
    public BigDecimal getModCompetenCiudadaPnal() { return modCompetenCiudadaPnal; }
    public void setModCompetenCiudadaPnal(BigDecimal modCompetenCiudadaPnal) { this.modCompetenCiudadaPnal = modCompetenCiudadaPnal; }
    public BigDecimal getModComuniEscritaPunt() { return modComuniEscritaPunt; }
    public void setModComuniEscritaPunt(BigDecimal modComuniEscritaPunt) { this.modComuniEscritaPunt = modComuniEscritaPunt; }
    public BigDecimal getModComuniEscritaPnbc() { return modComuniEscritaPnbc; }
    public void setModComuniEscritaPnbc(BigDecimal modComuniEscritaPnbc) { this.modComuniEscritaPnbc = modComuniEscritaPnbc; }
    public BigDecimal getModComuniEscritaPnal() { return modComuniEscritaPnal; }
    public void setModComuniEscritaPnal(BigDecimal modComuniEscritaPnal) { this.modComuniEscritaPnal = modComuniEscritaPnal; }
    public BigDecimal getModInglesPunt() { return modInglesPunt; }
    public void setModInglesPunt(BigDecimal modInglesPunt) { this.modInglesPunt = modInglesPunt; }
    public BigDecimal getModInglesPnbc() { return modInglesPnbc; }
    public void setModInglesPnbc(BigDecimal modInglesPnbc) { this.modInglesPnbc = modInglesPnbc; }
    public BigDecimal getModInglesPnal() { return modInglesPnal; }
    public void setModInglesPnal(BigDecimal modInglesPnal) { this.modInglesPnal = modInglesPnal; }
    public BigDecimal getModLecturaCriticaPunt() { return modLecturaCriticaPunt; }
    public void setModLecturaCriticaPunt(BigDecimal modLecturaCriticaPunt) { this.modLecturaCriticaPunt = modLecturaCriticaPunt; }
    public BigDecimal getModLecturaCriticaPnbc() { return modLecturaCriticaPnbc; }
    public void setModLecturaCriticaPnbc(BigDecimal modLecturaCriticaPnbc) { this.modLecturaCriticaPnbc = modLecturaCriticaPnbc; }
    public BigDecimal getModLecturaCriticaPnal() { return modLecturaCriticaPnal; }
    public void setModLecturaCriticaPnal(BigDecimal modLecturaCriticaPnal) { this.modLecturaCriticaPnal = modLecturaCriticaPnal; }
    public BigDecimal getModRazonaCuantitatPunt() { return modRazonaCuantitatPunt; }
    public void setModRazonaCuantitatPunt(BigDecimal modRazonaCuantitatPunt) { this.modRazonaCuantitatPunt = modRazonaCuantitatPunt; }
    public BigDecimal getModRazonaCuantitativoPnbc() { return modRazonaCuantitativoPnbc; }
    public void setModRazonaCuantitativoPnbc(BigDecimal modRazonaCuantitativoPnbc) { this.modRazonaCuantitativoPnbc = modRazonaCuantitativoPnbc; }
    public BigDecimal getModRazonaCuantitativoPnal() { return modRazonaCuantitativoPnal; }
    public void setModRazonaCuantitativoPnal(BigDecimal modRazonaCuantitativoPnal) { this.modRazonaCuantitativoPnal = modRazonaCuantitativoPnal; }
    public BigDecimal getPercentilGlobal() { return percentilGlobal; }
    public void setPercentilGlobal(BigDecimal percentilGlobal) { this.percentilGlobal = percentilGlobal; }
    public BigDecimal getPercentilNbc() { return percentilNbc; }
    public void setPercentilNbc(BigDecimal percentilNbc) { this.percentilNbc = percentilNbc; }
    public BigDecimal getPuntGlobal() { return puntGlobal; }
    public void setPuntGlobal(BigDecimal puntGlobal) { this.puntGlobal = puntGlobal; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

}
