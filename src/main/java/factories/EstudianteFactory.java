package factories;

import model.*;
import org.springframework.stereotype.Component;
import repository.EstudianteRepository;
import repository.InscripcionRepository;

import java.util.Map;

@Component
public class EstudianteFactory implements UserEspecializedFactory {
    private final EstudianteRepository  estudianteRepository;
    private final InscripcionRepository   inscripcionRepository;

    public EstudianteFactory(EstudianteRepository estudianteRepository, InscripcionRepository inscripcionRepository) {
        this.estudianteRepository = estudianteRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    public boolean supports(Usuario.rolType rol){
        return rol == Usuario.rolType.Estudiante;
    }

    @Override
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

            Inscripcion inscripcion = estudiante.getInscripcion();
            Programa nuevoPrograma = (Programa) datos.get("programa");

            if(inscripcion!=null && !inscripcion.getPrograma().equals(nuevoPrograma)){
                inscripcionRepository.delete(inscripcion);
                estudiante.setInscripcion(null);

                Inscripcion nuevaInscripcion  = new Inscripcion();
                InscripcionId nuevaInscripcionId = new InscripcionId(usuario.getId(),nuevoPrograma.getCodeProgram());

                nuevaInscripcion.setId(nuevaInscripcionId);
                nuevaInscripcion.setEstudiante(estudiante);
                nuevaInscripcion.setPrograma(nuevoPrograma);
                estudiante.setInscripcion(nuevaInscripcion);

                inscripcionRepository.save(nuevaInscripcion);
            }
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
