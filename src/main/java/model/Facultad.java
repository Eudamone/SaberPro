package model;

import jakarta.persistence.*;

@Entity
@Table(name = "facultad")
public class Facultad {
    @Id
    @Column(name = "cod_facultad", length = 20)
    private String codeFaculty;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String name;

    // Relación OneToOne con Decano:
    // El id_user de la facultad es una FK que apunta al PK del Decano.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private Decano decano;
}
