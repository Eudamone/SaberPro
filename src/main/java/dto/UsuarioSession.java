package dto;

import model.Usuario;

public record UsuarioSession(Long id, String username, String rol) {
    public Usuario.rolType getRol() {
        return Usuario.rolType.fromTipo(rol);
    }
}
