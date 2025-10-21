package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "resultadosaberpro")
public class ResultadoSaberPro {
    @Id
    private Long id;

    @Column(name = "numero_registro",length = 50,nullable = false,unique = true)
    private String num_registro;

    @Column(name = "anio",nullable = false)
    private Integer anio;

    @Column(name = "semestre",nullable = false)
    private Integer semestre;

    @Column(name = "percentil_nacional_global",nullable = true)
    private Integer pnal_glob;

    @Column(name = "puntaje_global")
    private Integer p_glob;

    @Column(name = "id_ciudad",nullable = false)
    private Long ciudad;

    // Llave foranea a la tabla estudiante
    @Column(name = "id_user",nullable = false)
    private String id_user;


    //Getters and Setters
    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public Long getCiudad() {
        return ciudad;
    }

    public void setCiudad(Long ciudad) {
        this.ciudad = ciudad;
    }

    public Integer getP_glob() {
        return p_glob;
    }

    public void setP_glob(Integer p_glob) {
        this.p_glob = p_glob;
    }

    public Integer getPnal_glob() {
        return pnal_glob;
    }

    public void setPnal_glob(Integer pnal_glob) {
        this.pnal_glob = pnal_glob;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getNum_registro() {
        return num_registro;
    }

    public void setNum_registro(String num_registro) {
        this.num_registro = num_registro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
