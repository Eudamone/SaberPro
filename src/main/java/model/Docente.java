package model;

import jakarta.persistence.*;

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


    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_docente", length = 20, nullable = false)
    private Decano.tipoDocente typeTeacher;
}
