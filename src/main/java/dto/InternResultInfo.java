package dto;

public class InternResultInfo {
    private Integer periodo;
    private String nombre;
    private String numeroRegistro;
    private String programa;
    private Integer ptjeGlobal;

    public InternResultInfo() {}

    public InternResultInfo(Integer periodo, String nombre, String numeroRegistro, String programa, Integer ptjeGlobal) {
        this.periodo = periodo;
        this.nombre = nombre;
        this.numeroRegistro = numeroRegistro;
        this.programa = programa;
        this.ptjeGlobal = ptjeGlobal;
    }

    public Integer getPeriodo() {
        return periodo;
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
}
