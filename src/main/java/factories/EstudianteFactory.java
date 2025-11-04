package factories;

import jakarta.transaction.Transactional;
import model.*;
import org.springframework.stereotype.Component;
import repository.EstudianteRepository;
import repository.InscripcionRepository;
import repository.ProgramaRepository;

import java.util.Map;

@Component
public class EstudianteFactory implements UserEspecializedFactory {
    private final EstudianteRepository  estudianteRepository;
    private final ProgramaRepository programaRepository;
    private final InscripcionRepository   inscripcionRepository;

    public EstudianteFactory(EstudianteRepository estudianteRepository, ProgramaRepository programaRepository, InscripcionRepository inscripcionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.programaRepository = programaRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    public boolean supports(Usuario.rolType rol){
        return rol == Usuario.rolType.Estudiante;
    }

    @Override
    @Transactional
    public void createEntityEspecialized(Usuario usuario, Map<String,Object> datos){
        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodeStudent((String) datos.get("codigo"));
        estudiante.setAcademicStatus(Estudiante.EstadoAcademico.valueOf((String) datos.get("estado")));
        estudianteRepository.save(estudiante);

        Programa programa = (Programa) datos.get("programa");

        Inscripcion inscripcion = new Inscripcion();
        InscripcionId inscripcionId = new InscripcionId(usuario.getId(),programa.getCodeProgram());

        inscripcion.setId(inscripcionId);
        inscripcion.setEstudiante(estudiante);
        inscripcion.setPrograma(programa);
        inscripcionRepository.save(inscripcion);
    }

    @Override
    public void updateEntityEspecialized(Usuario usuario, Map<String,Object> datos){
        Estudiante estudiante = usuario.getEstudiante();
        if(estudiante == null){
            createEntityEspecialized(usuario,datos);
        }else{
            estudiante.setCodeStudent((String) datos.get("codigo"));
            estudiante.setAcademicStatus(Estudiante.EstadoAcademico.valueOf((String) datos.get("estado")));
            estudiante.getInscripcion().setPrograma((Programa) datos.get("programa"));
            estudianteRepository.save(estudiante);
        }
    }

    @Override
    public void deleteEntityEspecialized(Usuario usuario){
        if(usuario.getEstudiante() != null){
            inscripcionRepository.delete(usuario.getEstudiante().getInscripcion());
            estudianteRepository.delete(usuario.getEstudiante());
            usuario.setEstudiante(null);
        }
    }
}
