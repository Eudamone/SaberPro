package factories;

import model.Usuario;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UsuarioSimpleFactory implements UserEspecializedFactory {

    @Override
    public boolean supports(Usuario.rolType rol){
        return rol == Usuario.rolType.Administrador || rol == Usuario.rolType.SecretariaAcreditacion;
    }

    @Override
    public void createEntityEspecialized(Usuario usuario, Map<String,Object> datos){
        // Esta implementación es vacía, pues es para usuarios sin más requisitos
    }

    @Override
    public void updateEntityEspecialized(Usuario usuario, Map<String,Object> datos){

    }

    @Override
    public void deleteEntityEspecialized(Usuario usuario){

    }
}
