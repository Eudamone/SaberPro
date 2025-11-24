package dto;

import java.util.ArrayList;
import java.util.List;

public class DatosRadarChart {
    private String programa;
    private List<ModuloPromedio> modulos;

    public DatosRadarChart(){
        this.programa = "";
        this.modulos = new ArrayList<>();
    }

    DatosRadarChart(String programa, List<ModuloPromedio> modulos) {
        this.programa = programa;
        this.modulos = modulos;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public List<ModuloPromedio> getModulos() {
        return modulos;
    }

    public void setModulos(List<ModuloPromedio> modulos) {
        this.modulos = modulos;
    }
}
