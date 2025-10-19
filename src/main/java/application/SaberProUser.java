package application;

import jakarta.persistence.*;

@Entity   //indica tabla
@Table(name = "usuario", schema = "saberpro")
public class SaberProUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)      //la PK id_user es autoincremental.
    @Column(name = "id_user")
    private Integer id;

    private String username;
    private String email;
    private String password; // <- aquí se guarda el HASH (bcrypt)
    private String nombre;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_identificacion")
    private String numeroIdentificacion;

    private String rol;

    @Column(name = "cuenta_activa")
    private Boolean cuentaActiva;

    // --- getters y setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getCuentaActiva() { return cuentaActiva; }
    public void setCuentaActiva(Boolean cuentaActiva) { this.cuentaActiva = cuentaActiva; }
}


