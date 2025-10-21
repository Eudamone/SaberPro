package model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "estudiante")
public class Estudiante {
    // Clave primaria compartida: El id es el mismo que el de Usuario.
    @Id
    @Column(name = "id_user")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    @MapsId // Copia el ID de la entidad 'usuario' al 'id' de Estudiante
    private Usuario usuario;

    @Column(name = "codigo_estudiante", length = 20, nullable = false, unique = true)
    private String codeStudent;

    public enum EstadoAcademico {
        Activo, Egresado, Retirado
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_academico", length = 20, nullable = false)
    private EstadoAcademico academicStatus;

    // Relación ManyToMany (Inversa) con Programa a través de Inscripcion
    @OneToMany(mappedBy = "estudiante")
    private Set<Inscripcion> inscripciones;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null && usuario.getDecano() != null) {

        }
    }

    public String getCodeStudent() {
        return codeStudent;
    }

    public void setCodeStudent(String codeStudent) {
        this.codeStudent = codeStudent;
    }

    public EstadoAcademico getAcademicStatus() {
        return academicStatus;
    }

    public void setAcademicStatus(EstadoAcademico academicStatus) {
        this.academicStatus = academicStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
