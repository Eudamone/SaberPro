package model;

import jakarta.persistence.*;

import java.util.List;
import java.util.Set;

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
    @JoinColumn(name = "id_user", nullable = true)
    private Decano decano;

    @OneToMany(mappedBy = "facultad",cascade = CascadeType.ALL,orphanRemoval = true,fetch =  FetchType.LAZY)
    private Set<Docente> docentes;

    @OneToMany(mappedBy = "facultad",cascade = CascadeType.ALL,orphanRemoval = true,fetch =  FetchType.LAZY)
    private Set<Programa> programas;

    public String getCodeFaculty() {
        return codeFaculty;
    }

    public void setCodeFaculty(String codeFaculty) {
        this.codeFaculty = codeFaculty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Decano getDecano() {
        return decano;
    }

    public void setDecano(Decano decano) {
        this.decano = decano;
        if(decano != null && decano.getFacultad() != this){
            decano.setFacultad(this);
        }
    }

    public Set<Docente> getDocentes() {
        return docentes;
    }

    public Set<Programa> getProgramas() {
        return programas;
    }
}
