package factories;

import model.Decano;
import model.Facultad;
import model.Usuario;
import org.springframework.stereotype.Component;
import repository.DecanoRepository;

import java.util.Map;

@Component
public class DecanoFactory implements  UserEspecializedFactory {

    private final DecanoRepository decanoRepository;

    public DecanoFactory(DecanoRepository decanoRepository) {
        this.decanoRepository = decanoRepository;
    }

    @Override
    public boolean supports(Usuario.rolType rol){
        return rol == Usuario.rolType.Decano;
    }

    @Override
    public void createEntityEspecialized(Usuario usuario, Map<String,Object> datos){
        Decano decano = new Decano();
        decano.setUsuario(usuario);
        decano.setCodeTeacher((String) datos.get("codigo"));
        decano.setTipoDocente(Decano.tipoDocente.fromEtiqueta((String) datos.get("tipo")));
        decano.setFacultad((Facultad) datos.get("facultad"));
        decanoRepository.save(decano);
    }

    @Override
    public void updateEntityEspecialized(Usuario usuario,Map<String,Object> datos){
        Decano decano = usuario.getDecano();
        if(decano == null){
            createEntityEspecialized(usuario,datos);
        }else{
            decano.setCodeTeacher((String) datos.get("codigo"));
            decano.setTipoDocente(Decano.tipoDocente.fromEtiqueta((String) datos.get("tipo")));
            decano.setFacultad((Facultad) datos.get("facultad"));
            decanoRepository.save(decano);
        }
    }

    @Override
    public void deleteEntityEspecialized(Usuario usuario){
        if(usuario.getDecano() != null){
            decanoRepository.delete(usuario.getDecano());
            usuario.setDecano(null);
        }
    }
}
