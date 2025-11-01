package model;

import jakarta.persistence.*;

@Entity
@Table(name = "departamento")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Short idDepartamento;

    @Column(name = "nombre")
    private String nombre;

    public Departamento() {}

    public Short getIdDepartamento() { return idDepartamento; }
    public void setIdDepartamento(Short idDepartamento) { this.idDepartamento = idDepartamento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
