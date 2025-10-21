package model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "programa")
public class Programa {

    @Id
    @Column(name = "cod_programa",length = 20)
    private String codeProgram;

    @Column(name = "snies", length = 20, nullable = false, unique = true)
    private String snies;

    @Column(name = "nombre", length = 150, nullable = false, unique = true)
    private String name;

    // FK a Usuario (id_user)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id_user",nullable = false)
    private DirectorPrograma director;

    // FK a Facultad (cod_facultad)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_facultad", nullable = false)
    private Facultad facultad;

    // Relación OneToMany (Inversa) con ComiteDePrograma
    @OneToOne(mappedBy = "programa", cascade = CascadeType.ALL, orphanRemoval = true)
    private ComitePrograma comiteDePrograma;

    // Relación ManyToMany (Inversa) con Estudiante a través de Inscripcion
    @OneToMany(mappedBy = "programa")
    private Set<Inscripcion> inscripciones;
}
