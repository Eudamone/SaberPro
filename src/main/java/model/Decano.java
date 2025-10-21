package model;

import jakarta.persistence.*;

@Entity
@Table(name = "decano")
public class Decano {
    @Id
    @Column(name = "id_user")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_user")
    private Usuario usuario;

    @Column (name = "codigo_docente", unique = true, nullable = false, length = 20)
    private String codeTeacher;

    public enum tipoDocente{
        PLANTA("Planta"),
        OCASIONAL("Ocasional"),
        CATEDRATICO("Catedrático");

        private final String etiqueta;
        tipoDocente(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    @Enumerated(EnumType.STRING) // Añadido para mapear el Enum correctamente
    @Column(name = "tipo_docente", length = 20, nullable = false) // Columna según SQL
    private tipoDocente tipoDocente;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    // Al establecer el usuario, también se debería establecer el decano en el usuario
    // para mantener la bidireccionalidad.
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null && usuario.getDecano() != this) {
            usuario.setDecano(this);
        }
    }

    public String getCodeTeacher() {
        return codeTeacher;
    }

    public void setCodeTeacher(String codeTeacher) {
        this.codeTeacher = codeTeacher;
    }

    public tipoDocente getTipoDocente() {
        return tipoDocente;
    }

    public void setTipoDocente(tipoDocente tipoDocente) {
        this.tipoDocente = tipoDocente;
    }
}
