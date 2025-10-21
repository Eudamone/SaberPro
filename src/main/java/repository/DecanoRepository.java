package repository;

import model.Decano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DecanoRepository extends JpaRepository<Decano, Long> {
    
}
