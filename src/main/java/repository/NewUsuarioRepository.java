package repository;

import model.NewUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewUsuarioRepository extends JpaRepository<NewUsuario,Long> {

}
