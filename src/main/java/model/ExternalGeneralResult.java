package model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

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
    private String estuSniesPrgmacademico;

    @Column(name = "inst_cod_institucion", nullable = true)
    private Integer instCodInstitucion;

    @Column(name = "inst_nombre_institucion", nullable = true)
    private String instNombreInstitucion;

    // módulos y percentiles: en la BD son integer; usamos Integer
    @Column(name = "mod_competen_ciudada_punt", nullable = true)
    private Integer modCompetenCiudadaPunt;

    @Column(name = "mod_competen_ciudada_pnbc", nullable = true)
    private Integer modCompetenCiudadaPnbc;

    @Column(name = "mod_competen_ciudada_pnal", nullable = true)
    private Integer modCompetenCiudadaPnal;

    @Column(name = "mod_comuni_escrita_punt", nullable = true)
    private Integer modComuniEscritaPunt;

    @Column(name = "mod_comuni_escrita_pnbc", nullable = true)
    private Integer modComuniEscritaPnbc;

    @Column(name = "mod_comuni_escrita_pnal", nullable = true)
    private Integer modComuniEscritaPnal;

    @Column(name = "mod_ingles_punt", nullable = true)
    private Integer modInglesPunt;

    @Column(name = "mod_ingles_pnbc", nullable = true)
    private Integer modInglesPnbc;

    @Column(name = "mod_ingles_pnal", nullable = true)
    private Integer modInglesPnal;

    @Column(name = "mod_lectura_critica_punt", nullable = true)
    private Integer modLecturaCriticaPunt;

    @Column(name = "mod_lectura_critica_pnbc", nullable = true)
    private Integer modLecturaCriticaPnbc;

    @Column(name = "mod_lectura_critica_pnal", nullable = true)
    private Integer modLecturaCriticaPnal;

    @Column(name = "mod_razona_cuantitat_punt", nullable = true)
    private Integer modRazonaCuantitatPunt;

    @Column(name = "mod_razona_cuantitativo_pnbc", nullable = true)
    private Integer modRazonaCuantitativoPnbc;

    @Column(name = "mod_razona_cuantitativo_pnal", nullable = true)
    private Integer modRazonaCuantitativoPnal;

    @Column(name = "percentil_global", nullable = true)
    private Integer percentilGlobal;

    @Column(name = "percentil_nbc", nullable = true)
    private Integer percentilNbc;

    @Column(name = "punt_global", nullable = true)
    private Integer puntGlobal;

    @Column(name = "created_at", nullable = true)
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
    public String getEstuInstDepartamento() { return estuInstDepartamento; }
    public void setEstuInstDepartamento(String estuInstDepartamento) { this.estuInstDepartamento = estuInstDepartamento; }
    public String getEstuInstMunicipio() { return estuInstMunicipio; }
    public void setEstuInstMunicipio(String estuInstMunicipio) { this.estuInstMunicipio = estuInstMunicipio; }
    public String getEstuNucleoPregrado() { return estuNucleoPregrado; }
    public void setEstuNucleoPregrado(String estuNucleoPregrado) { this.estuNucleoPregrado = estuNucleoPregrado; }
    public String getEstuPrgmAcademico() { return estuPrgmAcademico; }
    public void setEstuPrgmAcademico(String estuPrgmAcademico) { this.estuPrgmAcademico = estuPrgmAcademico; }
    public String getEstuSniesPrgmacademico() { return estuSniesPrgmacademico; }
    public void setEstuSniesPrgmacademico(String estuSniesPrgmacademico) { this.estuSniesPrgmacademico = estuSniesPrgmacademico; }
    public Integer getInstCodInstitucion() { return instCodInstitucion; }
    public void setInstCodInstitucion(Integer instCodInstitucion) { this.instCodInstitucion = instCodInstitucion; }
    public String getInstNombreInstitucion() { return instNombreInstitucion; }
    public void setInstNombreInstitucion(String instNombreInstitucion) { this.instNombreInstitucion = instNombreInstitucion; }
    public Integer getModCompetenCiudadaPunt() { return modCompetenCiudadaPunt; }
    public void setModCompetenCiudadaPunt(Integer modCompetenCiudadaPunt) { this.modCompetenCiudadaPunt = modCompetenCiudadaPunt; }
    public Integer getModCompetenCiudadaPnbc() { return modCompetenCiudadaPnbc; }
    public void setModCompetenCiudadaPnbc(Integer modCompetenCiudadaPnbc) { this.modCompetenCiudadaPnbc = modCompetenCiudadaPnbc; }
    public Integer getModCompetenCiudadaPnal() { return modCompetenCiudadaPnal; }
    public void setModCompetenCiudadaPnal(Integer modCompetenCiudadaPnal) { this.modCompetenCiudadaPnal = modCompetenCiudadaPnal; }
    public Integer getModComuniEscritaPunt() { return modComuniEscritaPunt; }
    public void setModComuniEscritaPunt(Integer modComuniEscritaPunt) { this.modComuniEscritaPunt = modComuniEscritaPunt; }
    public Integer getModComuniEscritaPnbc() { return modComuniEscritaPnbc; }
    public void setModComuniEscritaPnbc(Integer modComuniEscritaPnbc) { this.modComuniEscritaPnbc = modComuniEscritaPnbc; }
    public Integer getModComuniEscritaPnal() { return modComuniEscritaPnal; }
    public void setModComuniEscritaPnal(Integer modComuniEscritaPnal) { this.modComuniEscritaPnal = modComuniEscritaPnal; }
    public Integer getModInglesPunt() { return modInglesPunt; }
    public void setModInglesPunt(Integer modInglesPunt) { this.modInglesPunt = modInglesPunt; }
    public Integer getModInglesPnbc() { return modInglesPnbc; }
    public void setModInglesPnbc(Integer modInglesPnbc) { this.modInglesPnbc = modInglesPnbc; }
    public Integer getModInglesPnal() { return modInglesPnal; }
    public void setModInglesPnal(Integer modInglesPnal) { this.modInglesPnal = modInglesPnal; }
    public Integer getModLecturaCriticaPunt() { return modLecturaCriticaPunt; }
    public void setModLecturaCriticaPunt(Integer modLecturaCriticaPunt) { this.modLecturaCriticaPunt = modLecturaCriticaPunt; }
    public Integer getModLecturaCriticaPnbc() { return modLecturaCriticaPnbc; }
    public void setModLecturaCriticaPnbc(Integer modLecturaCriticaPnbc) { this.modLecturaCriticaPnbc = modLecturaCriticaPnbc; }
    public Integer getModLecturaCriticaPnal() { return modLecturaCriticaPnal; }
    public void setModLecturaCriticaPnal(Integer modLecturaCriticaPnal) { this.modLecturaCriticaPnal = modLecturaCriticaPnal; }
    public Integer getModRazonaCuantitatPunt() { return modRazonaCuantitatPunt; }
    public void setModRazonaCuantitatPunt(Integer modRazonaCuantitatPunt) { this.modRazonaCuantitatPunt = modRazonaCuantitatPunt; }
    public Integer getModRazonaCuantitativoPnbc() { return modRazonaCuantitativoPnbc; }
    public void setModRazonaCuantitativoPnbc(Integer modRazonaCuantitativoPnbc) { this.modRazonaCuantitativoPnbc = modRazonaCuantitativoPnbc; }
    public Integer getModRazonaCuantitativoPnal() { return modRazonaCuantitativoPnal; }
    public void setModRazonaCuantitativoPnal(Integer modRazonaCuantitativoPnal) { this.modRazonaCuantitativoPnal = modRazonaCuantitativoPnal; }
    public Integer getPercentilGlobal() { return percentilGlobal; }
    public void setPercentilGlobal(Integer percentilGlobal) { this.percentilGlobal = percentilGlobal; }
    public Integer getPercentilNbc() { return percentilNbc; }
    public void setPercentilNbc(Integer percentilNbc) { this.percentilNbc = percentilNbc; }
    public Integer getPuntGlobal() { return puntGlobal; }
    public void setPuntGlobal(Integer puntGlobal) { this.puntGlobal = puntGlobal; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

}
