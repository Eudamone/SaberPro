package model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarionuevo")
public class NewUsuario {
    @Id
    @Column(name = "id_user")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user")
    private Usuario usuario;

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
