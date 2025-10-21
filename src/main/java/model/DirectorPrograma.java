package model;

import jakarta.persistence.*;

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

    // Relación OneToOne Inversa (no propietaria) con Programa
    // Asumiendo que un director solo dirige UN programa a la vez.
    @OneToOne(mappedBy = "director")
    private Programa programa;
}

