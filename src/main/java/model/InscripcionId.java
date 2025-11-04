package model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class InscripcionId implements Serializable {
    @Column(name = "id_user")
    private Long idUser; // Corresponde al tipo de id_user en Estudiante/Usuario
    @Column(name = "cod_programa")
    private String codPrograma; // Corresponde al tipo de cod_programa en Programa

    // Constructor por defecto
    public InscripcionId() {}

    // Constructor con campos
    public InscripcionId(Long idUser, String codPrograma) {
        this.idUser = idUser;
        this.codPrograma = codPrograma;
    }

    //  implementación equals() y hashCode() para una clave compuesta
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InscripcionId that = (InscripcionId) o;
        return Objects.equals(idUser, that.idUser) &&
                Objects.equals(codPrograma, that.codPrograma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, codPrograma);
    }

    // Getters y Setters
    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getCodPrograma() {
        return codPrograma;
    }

    public void setCodPrograma(String codPrograma) {
        this.codPrograma = codPrograma;
    }
}
