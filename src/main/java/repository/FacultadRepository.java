package repository;

import model.Facultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultadRepository extends JpaRepository<Facultad, String> {
    @Query("""
        SELECT f FROM Facultad f
    """)
    List<Facultad> findAllFacultades();
}
