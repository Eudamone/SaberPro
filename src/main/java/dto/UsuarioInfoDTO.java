package dto;

import model.Usuario;

public class UsuarioInfoDTO {
  private Long id;
  private String username;
  private String name;
  private String email;
  private Usuario.typeDocument document;
  private String numIdentification;
  private Usuario.rolType rol;

  public UsuarioInfoDTO(Long id,String username,String nombre,String email,Usuario.typeDocument document,String numIdentification,Usuario.rolType rol) {
      this.id = id;
      this.username = username;
      this.name = nombre;
      this.email = email;
      this.document = document;
      this.numIdentification = numIdentification;
      this.rol = rol;
  }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Usuario.typeDocument getDocument() {
        return document;
    }

    public String getNumIdentification() {
        return numIdentification;
    }

    public Usuario.rolType getRol() {
        return rol;
    }
}
