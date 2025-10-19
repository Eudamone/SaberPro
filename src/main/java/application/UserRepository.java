package application;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<SaberProUser, Integer> {
    Optional<SaberProUser> findByUsername(String username);
}
//concecta Usuarion con la BD
