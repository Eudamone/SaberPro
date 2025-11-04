package model;

import jakarta.persistence.*;
import utils.TipoDocenteConverter;

@Entity
@Table(name = "directorprograma")
public class DirectorPrograma {
    @Id
    @Column(name = "id_user")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @MapsId // Clave primaria compartida con Usuario
    private Usuario usuario;

    @Column (name = "codigo_docente", unique = true, nullable = false, length = 20)
    private String codeTeacher;

    @Column(name = "tipo_docente", length = 20, nullable = false) // Columna según SQL
    @Convert(converter = TipoDocenteConverter.class)
    private Decano.tipoDocente tipoDocente;

    public String getCodeTeacher() {
        return codeTeacher;
    }

    public void setCodeTeacher(String codeTeacher) {
        this.codeTeacher = codeTeacher;
    }

    public Decano.tipoDocente getTipoDocente() {
        return tipoDocente;
    }

    public void setTipoDocente(Decano.tipoDocente tipoDocente) {
        this.tipoDocente = tipoDocente;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if(usuario != null && usuario.getDirectorPrograma() == null){
            usuario.setDirectorPrograma(this);
        }
    }
}

