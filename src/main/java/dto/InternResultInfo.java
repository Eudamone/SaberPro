package dto;

public class InternResultInfo {
    private Integer periodo;
    private Integer semestre;
    private String nombre;
    private String numeroRegistro;
    private String programa;
    private Integer ptjeGlobal;
    private String grupoReferencia;

    public InternResultInfo() {}

    public InternResultInfo(Integer periodo, Integer semestre, String nombre, String numeroRegistro, String programa, Integer ptjeGlobal, String grupoReferencia) {
        this.periodo = periodo;
        this.semestre = semestre;
        this.nombre = nombre;
        this.numeroRegistro = numeroRegistro;
        this.programa = programa;
        this.ptjeGlobal = ptjeGlobal;
        this.grupoReferencia = grupoReferencia;
    }

    public Integer getPeriodo() {
        return periodo;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    public void setPeriodo(Integer periodo) {
        this.periodo = periodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }

    public void setNumeroRegistro(String numeroRegistro) {
        this.numeroRegistro = numeroRegistro;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public Integer getPtjeGlobal() {
        return ptjeGlobal;
    }

    public void setPtjeGlobal(Integer ptjeGlobal) {
        this.ptjeGlobal = ptjeGlobal;
    }

    public String getGrupoReferencia() {
        return grupoReferencia;
    }

    public void setGrupoReferencia(String grupoReferencia) {
        this.grupoReferencia = grupoReferencia;
    }
}
