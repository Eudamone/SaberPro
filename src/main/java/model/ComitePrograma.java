package model;

import jakarta.persistence.*;

@Entity
@Table(name = "comitedeprograma")
public class ComitePrograma {
    @Id
    @Column(name = "id_comite", length = 20)
    private String idComite;

    // 1. Relación OneToOne con Programa (EL COMITÉ ES EL PROPIETARIO DE LA FK)
    // La columna 'cod_programa' es la FK que apunta al PK del Programa.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cod_programa", nullable = false)
    private Programa programa;

}
