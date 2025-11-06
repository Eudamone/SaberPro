package dto;


import com.poiji.annotation.ExcelCell;
import model.Usuario;

public class EstudianteLoad {

    @ExcelCell(0)
    private String nombre;

    @ExcelCell(1)
    private String email;

    @ExcelCell(2)
    private Usuario.typeDocument typeDocument;

    @ExcelCell(3)
    private String numDocument;

    @ExcelCell(4)
    private String codEstudent;

    @ExcelCell(5)
    private String nombrePrograma;

    public EstudianteLoad(){}

    public EstudianteLoad(String nombre, String email, Usuario.typeDocument typeDocument, String numDocument, String codEstudent, String nombrePrograma) {
        this.nombre = nombre;
        this.email = email;
        this.typeDocument = typeDocument;
        this.numDocument = numDocument;
        this.codEstudent = codEstudent;
        this.nombrePrograma = nombrePrograma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Usuario.typeDocument getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(Usuario.typeDocument typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getNumDocument() {
        return numDocument;
    }

    public void setNumDocument(String numDocument) {
        this.numDocument = numDocument;
    }

    public String getCodEstudent() {
        return codEstudent;
    }

    public void setCodEstudent(String codEstudent) {
        this.codEstudent = codEstudent;
    }

    public String getNombrePrograma() {
        return nombrePrograma;
    }

    public void setNombrePrograma(String nombrePrograma) {
        this.nombrePrograma = nombrePrograma;
    }
}
