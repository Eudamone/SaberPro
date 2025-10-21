package model;

import jakarta.persistence.*;



@Entity
@Table( name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "email",length = 100,nullable = false, unique = true)
    private String email;

    @Column(name = "password",length = 255,nullable = false)
    private String pass; // Aqui realmente se guarda un hash

    @Column(name = "nombre",length = 150,nullable = false)
    private String nombre;

    public enum typeDocument{
        CC, TI, CE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento",length = 10,nullable = false)
    private typeDocument document ;

    @Column(name = "numero_identificacion",length = 20,nullable = false,unique = true)
    private String numIdentification;

    public enum rolType{
        Administrador("Administrador"),
        Estudiante("Estudiante"),
        Decano("Decano"),
        DirectorPrograma("Director Programa"),
        CoordinadorPrograma("Coordinador Programa"),
        SecretariaAcreditacion("Secretaria Acreditacion"),
        Docente("Docente");

        private final String tipo;
        rolType(String tipo){
            this.tipo = tipo;
        }

        public String getTipo() {
            return tipo;
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "rol",length = 30,nullable = false)
    private rolType rol;

    // Relación OneToOne Inversa (no propietaria) con Decano.
    // Usamos mappedBy para indicar que la relación es manejada por el campo 'usuario' en Decano.
    // Usamos CascadeType.ALL para simplificar la persistencia/eliminación.
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL)
    private Decano decano;

    @OneToOne(mappedBy = "usuario",cascade = CascadeType.ALL)
    private Estudiante estudiante;

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
        if(estudiante != null){
            estudiante.setUsuario(this);
        }
    }

    public Decano getDecano() {
        return decano;
    }

    public void setDecano(Decano decano) {
        this.decano = decano;
        if (decano != null) {
            decano.setUsuario(this);
        }
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public typeDocument getDocument() {
        return document;
    }

    public void setDocument(typeDocument document) {
        this.document = document;
    }

    public String getNumIdentification() {
        return numIdentification;
    }

    public void setNumIdentification(String numIdentification) {
        this.numIdentification = numIdentification;
    }

    public String getRol() {
        return rol.toString();
    }

    public void setRol(rolType rol) {
        this.rol = rol;
    }
}
