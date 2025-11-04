package factories;

import model.Decano;
import model.Docente;
import model.Facultad;
import model.Usuario;
import org.springframework.stereotype.Component;
import repository.DocenteRepository;
import repository.FacultadRepository;

import java.util.Map;
import java.util.Optional;

@Component
public class DocenteFactory implements UserEspecializedFactory{

    private final DocenteRepository docenteRepository;
    private final FacultadRepository facultadRepository;

    public DocenteFactory(DocenteRepository docenteRepository, FacultadRepository facultadRepository) {
        this.docenteRepository = docenteRepository;
        this.facultadRepository = facultadRepository;
    }

    @Override
    public boolean supports(Usuario.rolType rol){
        return rol == Usuario.rolType.Docente;
    }

    @Override
    public void createEntityEspecialized(Usuario usuario, Map<String,Object> datos){
        Docente docente = new Docente();
        docente.setUsuario(usuario);
        docente.setCodeTeacher((String) datos.get("codigo"));
        docente.setTypeTeacher(Decano.tipoDocente.fromEtiqueta((String) datos.get("tipo")));
        Facultad facultad = (Facultad) datos.get("facultad");
        docente.setFacultad(facultad);
        docenteRepository.save(docente);
    }

    @Override
    public void updateEntityEspecialized(Usuario usuario, Map<String,Object> datos){
        Docente docente = usuario.getDocente();
        if(docente == null){
            createEntityEspecialized(usuario,datos);
        }else{
            docente.setCodeTeacher((String) datos.get("codigo"));
            docente.setTypeTeacher(Decano.tipoDocente.fromEtiqueta((String) datos.get("tipo")));
            docente.setFacultad((Facultad) datos.get("facultad"));
            docenteRepository.save(docente);
        }
    }

    @Override
    public void deleteEntityEspecialized(Usuario usuario){
        if(usuario.getDocente() != null){
            docenteRepository.delete(usuario.getDocente());
            usuario.setDocente(null);
        }
    }
}
