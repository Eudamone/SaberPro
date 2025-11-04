package model;

import jakarta.persistence.*;
import utils.TipoDocenteConverter;

@Entity
@Table(name = "docente")
public class Docente {
    // Clave primaria compartida con Usuario
    @Id
    @Column(name = "id_user")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @MapsId
    private Usuario usuario;

    @Column(name = "codigo_docente", length = 20, nullable = false, unique = true)
    private String codeTeacher;


    @Column(name = "tipo_docente", length = 20, nullable = false)
    @Convert(converter = TipoDocenteConverter.class)
    private Decano.tipoDocente typeTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_facultad",nullable = false)
    private Facultad facultad;

    public Usuario  getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if(usuario != null && usuario.getDocente() == null){
            usuario.setDocente(this);
        }
    }

    public String getCodeTeacher() {
        return codeTeacher;
    }

    public void setCodeTeacher(String codeTeacher) {
        this.codeTeacher = codeTeacher;
    }

    public Decano.tipoDocente getTypeTeacher() {
        return typeTeacher;
    }

    public void setTypeTeacher(Decano.tipoDocente typeTeacher) {
        this.typeTeacher = typeTeacher;
    }

    public Long getId() {
        return id;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    public Facultad getFacultad() {
        return facultad;
    }

}
