package factories;

import model.Usuario;

import java.util.Map;

public interface UserEspecializedFactory {
    void createEntityEspecialized(Usuario usuario, Map<String,Object> datos);
    boolean supports(Usuario.rolType rol);
    void updateEntityEspecialized(Usuario usuario, Map<String,Object> datos);
    void deleteEntityEspecialized(Usuario usuario);
}
