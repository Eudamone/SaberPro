package model;

import jakarta.persistence.*;
import repository.ProgramaRepository;

@Entity
@Table(name = "inscripcion")
public class Inscripcion {
    // Uso de la clase embebida como clave primaria
    @EmbeddedId
    private InscripcionId id;


    // FK a Estudiante (id_user)
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @MapsId("idUser") // Mapea el campo 'idUser' del EmbeddedId
    @JoinColumn(name = "id_user", nullable = false)
    private Estudiante estudiante;

    // FK a Programa (cod_programa)
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("codPrograma") // Mapea el campo 'codPrograma' del EmbeddedId
    @JoinColumn(name = "cod_programa", nullable = false)
    private Programa programa;

    // Getters y Setters
    public InscripcionId getId() {
        return id;
    }

    public void setId(InscripcionId id) {
        this.id = id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Programa getPrograma() {
        return programa;
    }

    public void setPrograma(Programa programa) {
        this.programa = programa;
    }
}
