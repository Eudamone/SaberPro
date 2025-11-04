package factories;

import model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UsuarioFactoryRegistry {
    private final List<UserEspecializedFactory> factories;

    public UsuarioFactoryRegistry(List<UserEspecializedFactory> factories) {
        this.factories = factories;
    }

    public void createUserSpecificFactory(Usuario usuario,Map<String,Object> datos) {
        factories.stream()
                .filter(f -> f.supports(usuario.getRol()))
                .findFirst()
                .ifPresent(f -> f.createEntityEspecialized(usuario,datos));
    }

    public void updateUserSpecificFactory(Usuario usuario,Map<String,Object> datos) {
        factories.stream().
                filter(f -> f.supports(usuario.getRol()))
                .findFirst()
                .ifPresent(f -> f.updateEntityEspecialized(usuario,datos));
    }

    public void deleteUserSpecificFactory(Usuario usuario,String rolAnterior) {
        factories.stream().
                filter(f -> f.supports(Usuario.rolType.fromTipo(rolAnterior))).
                findFirst().
                ifPresent(f -> f.deleteEntityEspecialized(usuario));
    }
}
