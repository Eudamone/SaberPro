package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ResultadoSaberProExterno {
    @Id
    private Long id;

    @Column
    private String periodo;

    @Column
    private String est_consecutivo;

    @Column
    private Integer inst_cod_institucion;

    @Column
    private String inst_nombre_institucion;

    @Column
    private String estu_inst_departamento;

    @Column
    private String estu_inst_municipio;

    @Column
    private String estu_nucleo_pregrado;

    @Column
    private String prgm_academico;

    @Column
    private String snies_prgm_academico;


}
